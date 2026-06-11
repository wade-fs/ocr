# History

**2026-06-10** - 完成以下功能：

1. **首頁即名片管理** – `MainActivity` 設為 `NavHostFragment`，起始畫面為 `CardListFragment`。
2. **預建五個分類** – `personal、work、friend、family、former_colleague`，在資料庫 `CategoryEntity` 中建立，`CardEntity.category` 預設為 `personal`。
3. **辨識後直接加入** – FAB 觸發相機 → OCR → 自動寫入 `CardEntity`（預設個人分類）。
4. **重新辨識** – `CardDetailFragment` 提供「重新辨識」按鈕，重新執行 OCR 並覆寫同筆資料。
5. **資料庫層** – 新增 `CategoryEntity`、`CategoryDao`、`AppDatabase` 整合、`CardRepository` 內加入分類相關方法。
6. **ViewModel** – 在 `CardViewModel` 初始化時插入預設分類；提供 CRUD、搬移、重新辨識等 API。
7. **UI 元件** – 
   - `CardListFragment`（清單、拖曳改分類、搜尋、FAB）
   - `CardDetailFragment`（顯示詳細、重新辨識）
   - `CardEditFragment`（編輯/新增，含分類 Spinner）
   - `CardAdapter`、`item_card.xml`、相關 layout 與 navigation 設定。
8. **依賴** – `room-runtime`, `room-ktx`, `kapt`, `gson`, `lifecycle-viewmodel-ktx`, `recyclerview` 已加入 `build.gradle.kts`。

以上變更已同步至專案路徑 `/app/src/main/java/com/wade/ocr/...`，可直接編譯執行。

**2026-06-11** - 完成以下功能：

1. **欄位空白處理** – 優化 `CardExtractor` 與編輯邏輯，若掃瞄未找到資訊則保持空白，不填入預設值。
2. **自定義欄位支援** – 
   - `BusinessCard` 與 `CardEntity` 新增 `customFields` / `custom_fields_json` 欄位。
   - `Converters.kt` 增加 Map<String, String> 的 JSON 轉換支援。
3. **編輯介面優化** – 
   - 補全 `WeChat` 與 `Line ID` 的編輯輸入框。
   - 實作動態 UI：可點擊「+ 新增欄位」隨意增加 IG、Messenger 等自定義資訊。
   - 支援動態移除自定義欄位。
4. **資料庫一致性** – `CardEntity` 補齊 `line` 欄位，確保掃瞄與編輯流程資料流完整。
5. **互動功能增強** – 
   - 在電話、電子郵件、網址欄位增加互動圖示。
   - 點擊電話圖示可直接撥號（Intent.ACTION_DIAL）。
   - 點擊郵件圖示可開啟寫信畫面（Intent.ACTION_SENDTO）。
   - 點擊網址圖示可直接開啟瀏覽器（Intent.ACTION_VIEW）。
   - 點擊 Line 圖示可嘗試直接跳轉至該 ID 的好友加好友/對話畫面（line://ti/p/~）。
   - 點擊 WeChat 圖示可自動開啟 WeChat App。

以上變更已同步至 `EditCardFragment.kt`、`CardEntity.kt`、`fragment_edit_card.xml` 等相關檔案。
