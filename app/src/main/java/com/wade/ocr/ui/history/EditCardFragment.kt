package com.wade.ocr.ui.history

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wade.ocr.R
import com.wade.ocr.data.CardEntity
import com.wade.ocr.data.local.AppDatabase
import com.wade.ocr.data.local.CategoryEntity
import com.wade.ocr.data.model.BusinessCard
import com.wade.ocr.data.model.PhoneEntry
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.wade.ocr.databinding.FragmentEditCardBinding
import com.wade.ocr.databinding.ItemCustomFieldBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditCardFragment : Fragment() {

    private var _binding: FragmentEditCardBinding? = null
    private val binding get() = _binding!!
    
    private var cardId: Long = -1L
    private var scannedCard: BusinessCard? = null
    private var currentDbCard: CardEntity? = null
    private var categories: List<CategoryEntity> = emptyList()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardId = arguments?.getLong("cardId", -1L) ?: -1L
        scannedCard = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("businessCard", BusinessCard::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("businessCard")
        }

        setupCategories()

        if (cardId != -1L) {
            loadCard()
            binding.buttonDelete.visibility = View.VISIBLE
        } else {
            binding.buttonDelete.visibility = View.GONE
            // If it's a new card, populate from scanned data
            scannedCard?.let { populateFromBusinessCard(it) }
        }

        binding.buttonSave.setOnClickListener { saveCard() }
        binding.buttonReRecognize.setOnClickListener { reRecognize() }
        binding.buttonDelete.setOnClickListener { deleteCard() }
        binding.buttonAddCategory.setOnClickListener { showAddCategoryDialog() }
        binding.buttonManageCategory.setOnClickListener { showManageCategoryDialog() }
        binding.buttonAddCustomField.setOnClickListener { addCustomFieldRow("", "") }

        setupInteractiveIcons()
    }

    private fun setupInteractiveIcons() {
        binding.layoutPhones.setEndIconOnClickListener {
            val phone = binding.editPhones.text.toString().split(",").firstOrNull { it.isNotBlank() }?.trim()
            if (!phone.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                startActivity(intent)
            }
        }

        binding.layoutEmails.setEndIconOnClickListener {
            val email = binding.editEmails.text.toString().split(",").firstOrNull { it.isNotBlank() }?.trim()
            if (!email.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                startActivity(intent)
            }
        }

        binding.layoutAddress.setEndIconOnClickListener {
            val address = binding.editAddress.text.toString().trim()
            if (address.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$address"))
                startActivity(intent)
            }
        }

        binding.layoutWebsite.setEndIconOnClickListener {
            var url = binding.editWebsite.text.toString().trim()
            if (url.isNotEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }

        binding.layoutWechat.setEndIconOnClickListener {
            val wechatId = binding.editWechat.text.toString().trim()
            if (wechatId.isNotEmpty()) {
                // WeChat doesn't support a direct user search URI easily anymore.
                // Best effort is opening the app or using weixin://
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("weixin://"))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "未安裝 WeChat", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.layoutLine.setEndIconOnClickListener {
            val lineId = binding.editLine.text.toString().trim()
            if (lineId.isNotEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("line://ti/p/~$lineId"))
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://line.me/ti/p/~$lineId"))
                    startActivity(intent)
                }
            }
        }
    }

    private fun addCustomFieldRow(key: String, value: String) {
        val fieldBinding = ItemCustomFieldBinding.inflate(layoutInflater, binding.containerCustomFields, false)
        fieldBinding.editKey.setText(key)
        fieldBinding.editValue.setText(value)
        fieldBinding.buttonRemove.setOnClickListener {
            binding.containerCustomFields.removeView(fieldBinding.root)
        }
        binding.containerCustomFields.addView(fieldBinding.root)
    }

    private fun showManageCategoryDialog() {
        if (categories.isEmpty()) return
        val categoryNames = categories.map { it.name }.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle("管理群組分類")
            .setItems(categoryNames) { _, which ->
                val selectedCategory = categories[which]
                showCategoryOptionsDialog(selectedCategory)
            }
            .setNegativeButton("關閉", null)
            .show()
    }

    private fun showCategoryOptionsDialog(category: CategoryEntity) {
        val options = arrayOf("更名", "刪除")
        AlertDialog.Builder(requireContext())
            .setTitle(category.name)
            .setItems(options) { _, which ->
                if (which == 0) {
                    showRenameCategoryDialog(category)
                } else if (which == 1) {
                    deleteCategory(category)
                }
            }
            .show()
    }

    private fun showRenameCategoryDialog(category: CategoryEntity) {
        val input = EditText(requireContext())
        input.setText(category.name)
        
        AlertDialog.Builder(requireContext())
            .setTitle("群組更名")
            .setView(input)
            .setPositiveButton("儲存") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != category.name) {
                    val dao = AppDatabase.getInstance(requireContext()).categoryDao()
                    lifecycleScope.launch(Dispatchers.IO) {
                        dao.rename(category.id, newName)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "已更名為 $newName", Toast.LENGTH_SHORT).show()
                            setupCategories()
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteCategory(category: CategoryEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("確認刪除群組")
            .setMessage("確定要刪除「${category.name}」嗎？此群組內的名片仍會保留，但分類可能變為空白或預設。")
            .setPositiveButton("刪除") { _, _ ->
                val dao = AppDatabase.getInstance(requireContext()).categoryDao()
                lifecycleScope.launch(Dispatchers.IO) {
                    dao.delete(category.id)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "已刪除群組", Toast.LENGTH_SHORT).show()
                        setupCategories()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun setupCategories() {
        val categoryDao = AppDatabase.getInstance(requireContext()).categoryDao()
        lifecycleScope.launch(Dispatchers.IO) {
            categories = categoryDao.getAll().first()
            val categoryNames = categories.map { it.name }
            
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategory.adapter = adapter
                
                // If editing existing, wait for loadCard to set selection
                // If creating new, set default "個人"
                if (cardId == -1L) {
                    val index = categoryNames.indexOf("個人")
                    if (index >= 0) binding.spinnerCategory.setSelection(index)
                }
            }
        }
    }

    private fun loadCard() {
        val dao = AppDatabase.getInstance(requireContext()).cardDao()
        lifecycleScope.launch(Dispatchers.IO) {
            currentDbCard = dao.getById(cardId)
            withContext(Dispatchers.Main) {
                // If we returned from re-recognize, scannedCard will not be null, use it instead of DB fields for text
                if (scannedCard != null) {
                    populateFromBusinessCard(scannedCard!!)
                } else {
                    currentDbCard?.let { populateFromDbCard(it) }
                }

                // Set Category from DB
                currentDbCard?.let { card ->
                    val index = categories.indexOfFirst { it.name == card.category }
                    if (index >= 0) {
                        binding.spinnerCategory.setSelection(index)
                    }
                }
            }
        }
    }

    private fun populateFromDbCard(card: CardEntity) {
        binding.editName.setText(card.name)
        binding.editTitle.setText(card.title)
        binding.editCompany.setText(card.company)
        binding.editAddress.setText(card.address)
        binding.editWebsite.setText(card.website)
        binding.editWechat.setText(card.wechat)
        binding.editLine.setText(card.line)
        binding.editNote.setText(card.rawText)
        
        val phonesType = object : TypeToken<List<PhoneEntry>>() {}.type
        val phonesList: List<PhoneEntry>? = card.phonesJson?.let { gson.fromJson(it, phonesType) }
        binding.editPhones.setText(phonesList?.joinToString(", ") { it.number ?: "" })
        
        val emailsType = object : TypeToken<List<String>>() {}.type
        val emailsList: List<String>? = card.emailsJson?.let { gson.fromJson(it, emailsType) }
        binding.editEmails.setText(emailsList?.joinToString(", "))

        // Custom Fields
        binding.containerCustomFields.removeAllViews()
        val customFieldsType = object : TypeToken<Map<String, String>>() {}.type
        val customFields: Map<String, String>? = card.customFieldsJson?.let { gson.fromJson(it, customFieldsType) }
        customFields?.forEach { (k, v) -> addCustomFieldRow(k, v) }
        
        card.imagePath?.let { loadCardImage(it) }
        
        binding.textRawOcr.text = "原始 OCR:\n${card.rawText ?: "無"}"
    }

    private fun loadCardImage(path: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeFile(path)
                withContext(Dispatchers.Main) {
                    binding.imageCard.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                // Ignore loading error
            }
        }
    }

    private fun populateFromBusinessCard(card: BusinessCard) {
        binding.editName.setText(card.name)
        binding.editTitle.setText(card.title)
        binding.editCompany.setText(card.company)
        binding.editAddress.setText(card.address)
        binding.editWebsite.setText(card.website)
        binding.editWechat.setText(card.wechat)
        binding.editLine.setText(card.line)
        binding.editNote.setText(card.note)
        
        binding.editPhones.setText(card.phones?.joinToString(", ") { it.number ?: "" })
        binding.editEmails.setText(card.emails?.joinToString(", "))

        // Custom Fields
        binding.containerCustomFields.removeAllViews()
        card.customFields?.forEach { (k, v) -> addCustomFieldRow(k, v) }
        
        card.imagePath?.let { loadCardImage(it) }
        
        binding.textRawOcr.text = "原始 OCR:\n${card.note ?: "無"}"
    }

    private fun saveCard() {
        val selectedCategory = binding.spinnerCategory.selectedItem as? String ?: "個人"
        val nameInput = binding.editName.text.toString().takeIf { it.isNotBlank() }

        // Convert comma separated phones to JSON
        val phoneStrings = binding.editPhones.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val phoneEntries = phoneStrings.map { PhoneEntry(type = null, number = it) }
        
        val emailStrings = binding.editEmails.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // Gather Custom Fields
        val customFields = mutableMapOf<String, String>()
        for (i in 0 until binding.containerCustomFields.childCount) {
            val child = binding.containerCustomFields.getChildAt(i)
            val fieldBinding = ItemCustomFieldBinding.bind(child)
            val key = fieldBinding.editKey.text.toString().trim()
            val value = fieldBinding.editValue.text.toString().trim()
            if (key.isNotEmpty()) {
                customFields[key] = value
            }
        }

        val cardEntity = CardEntity(
            id = if (cardId != -1L) cardId else 0L,
            name = nameInput,
            title = binding.editTitle.text.toString().takeIf { it.isNotBlank() },
            company = binding.editCompany.text.toString().takeIf { it.isNotBlank() },
            address = binding.editAddress.text.toString().takeIf { it.isNotBlank() },
            website = binding.editWebsite.text.toString().takeIf { it.isNotBlank() },
            category = selectedCategory,
            phonesJson = if (phoneEntries.isNotEmpty()) gson.toJson(phoneEntries) else null,
            emailsJson = if (emailStrings.isNotEmpty()) gson.toJson(emailStrings) else null,
            rawText = binding.editNote.text.toString().takeIf { it.isNotBlank() } ?: currentDbCard?.rawText,
            wechat = binding.editWechat.text.toString().takeIf { it.isNotBlank() },
            line = binding.editLine.text.toString().takeIf { it.isNotBlank() },
            bboxJson = currentDbCard?.bboxJson,
            customFieldsJson = if (customFields.isNotEmpty()) gson.toJson(customFields) else null,
            imagePath = scannedCard?.imagePath ?: currentDbCard?.imagePath
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(requireContext()).cardDao()

            // If new card, check duplicate name
            if (cardId == -1L && nameInput != null) {
                val duplicate = dao.getByName(nameInput)
                if (duplicate != null) {
                    withContext(Dispatchers.Main) {
                        promptForCategoryAndSave(cardEntity, nameInput)
                    }
                    return@launch
                }
            }

            if (cardId != -1L) {
                dao.update(cardEntity)
            } else {
                dao.insert(cardEntity)
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "已儲存", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editCardFragment_to_historyFragment)
            }
        }
    }

    private fun promptForCategoryAndSave(cardEntity: CardEntity, duplicateName: String) {
        val categoryNames = categories.map { it.name }
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("發現同名名片 ($duplicateName)")
        builder.setMessage("資料庫中已有同名名片，請選擇要將此新名片歸類至哪個群組以利區分：")
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryNames)
        val spinner = android.widget.Spinner(requireContext()).apply {
            this.adapter = adapter
        }
        
        builder.setView(spinner)
        builder.setPositiveButton("儲存") { _, _ ->
            val selectedCategory = spinner.selectedItem as? String ?: "個人"
            val updatedEntity = cardEntity.copy(category = selectedCategory)
            
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getInstance(requireContext()).cardDao()
                dao.insert(updatedEntity)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "已儲存", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editCardFragment_to_historyFragment)
                }
            }
        }
        builder.setNegativeButton("取消", null)
        builder.show()
    }

    private fun deleteCard() {
        val card = currentDbCard ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("確認刪除")
            .setMessage("確定要刪除這張名片嗎？")
            .setPositiveButton("刪除") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(requireContext()).cardDao()
                    dao.delete(card)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "已刪除名片", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun reRecognize() {
        val bundle = Bundle().apply { putLong("cardId", cardId) }
        findNavController().navigate(R.id.action_editCardFragment_to_cameraFragment, bundle)
    }

    private fun showQrCodeDialog() {
        val name = binding.editName.text.toString().trim()
        val title = binding.editTitle.text.toString().trim()
        val company = binding.editCompany.text.toString().trim()
        val phone = binding.editPhones.text.toString().trim()
        val email = binding.editEmails.text.toString().trim()
        val website = binding.editWebsite.text.toString().trim()
        val wechat = binding.editWechat.text.toString().trim()
        val line = binding.editLine.text.toString().trim()

        // Create a simple text format for QR exchange. 
        // Using a custom prefix to identify it's an OCR app card
        val qrContent = StringBuilder().apply {
            appendLine("OCR_CARD_V1")
            if (name.isNotEmpty()) appendLine("N:$name")
            if (title.isNotEmpty()) appendLine("T:$title")
            if (company.isNotEmpty()) appendLine("C:$company")
            if (phone.isNotEmpty()) appendLine("P:$phone")
            if (email.isNotEmpty()) appendLine("E:$email")
            if (website.isNotEmpty()) appendLine("W:$website")
            if (wechat.isNotEmpty()) appendLine("WC:$wechat")
            if (line.isNotEmpty()) appendLine("L:$line")
        }.toString()

        try {
            val writer = MultiFormatWriter()
            val matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 600, 600)
            val encoder = BarcodeEncoder()
            val bitmap = encoder.createBitmap(matrix)

            val imageView = android.widget.ImageView(requireContext()).apply {
                setImageBitmap(bitmap)
                setPadding(40, 40, 40, 40)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("掃瞄此 QR Code 以交換名片")
                .setView(imageView)
                .setPositiveButton("關閉", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "無法產生 QR Code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddCategoryDialog() {
        val input = EditText(requireContext())
        input.hint = "輸入新分類名稱"
        
        AlertDialog.Builder(requireContext())
            .setTitle("新增群組分類")
            .setView(input)
            .setPositiveButton("新增") { _, _ ->
                val newCategory = input.text.toString().trim()
                if (newCategory.isNotEmpty()) {
                    val dao = AppDatabase.getInstance(requireContext()).categoryDao()
                    lifecycleScope.launch(Dispatchers.IO) {
                        val existing = dao.getByName(newCategory)
                        if (existing == null) {
                            dao.insert(CategoryEntity(name = newCategory))
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "已新增 $newCategory", Toast.LENGTH_SHORT).show()
                                setupCategories()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "分類已存在", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
