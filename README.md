# OCR 名片管理應用

## 目標
- 提供一個以 **Room** 為後端的名片管理系統，支援:
  - 首頁即名片清單（`CardListFragment`）
  - 五個預設分類：`personal、work、friend、family、former_colleague`
  - 辨識後直接加入（預設 `personal`）
  - 卡片 **重新辨識** 功能（`CardDetailFragment`）
  - 拖曳改分類、刪除、編輯

## 主要功能說明
1. **首頁即管理**
   - `MainActivity` 設為 `NavHostFragment`，起始畫面為 `CardListFragment`。
   - 右下角 **FAB** → 開啟相機 → OCR → 產生 `CardEntity` 並寫入資料庫（`category` 預設 `personal`）。
2. **分類**
   - `CategoryEntity` 與 `CategoryDao` 提供固定五種分類，啟動時自動插入。
   - `CardListFragment` 支援長按拖曳改分類，使用 `CardViewModel.move(cardId, newCategory)` 更新。
3. **重新辨識**
   - 點選清單任一卡片進入 `CardDetailFragment`，點擊 **重新辨識** 按鈕會重新跑 OCR，結果覆寫同筆資料。
4. **編輯/新增**
   - `CardEditFragment` 包含所有欄位的 `EditText`，分類使用 `Spinner` 讀取 `categories`。
   - `save` 後透過 `CardViewModel.insert` 或 `update` 寫入 DB。
5. **資料庫**
   - `AppDatabase` 包含 `CardEntity`、`CategoryEntity`，使用 `Room`、`Gson` 轉換 `List` 欄位。
   - `CardRepository` 提供 CRUD、分類相關 API，`CardViewModel` 將其包裝為 `StateFlow` 供 UI 觀測。

## 建置步驟
```bash
# 1. 下載專案 (已在 /home/wade/src/github/OCR)
cd /home/wade/src/github/OCR

# 2. 確認 Gradle 依賴已加入（Room、Gson、Lifecycle 等）
# 3. 編譯 Debug 版
./gradlew assembleDebug

# 4. 安裝到裝置或模擬器
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

執行後會直接看到名片清單畫面，使用 FAB 新增、長按拖曳改分類、點卡片可編輯或重新辨識。

## 目錄結構（新增檔案）
```
app/src/main/java/com/wade/ocr/data/local/
│   CategoryEntity.kt   # 預設分類實體
│   CategoryDao.kt      # 操作分類表
│   CardDao.kt          # 原有卡片 DAO（加 moveCard）
│   Converters.kt       # List ↔ JSON 轉換
│   AppDatabase.kt      # 包含 Card & Category

app/src/main/java/com/wade/ocr/data/repository/
│   CardRepository.kt   # 包含分類相關方法

app/src/main/java/com/wade/ocr/ui/viewmodel/
│   CardViewModel.kt    # 初始化預建分類、提供 CRUD/移動/重新辨識

app/src/main/java/com/wade/ocr/ui/fragment/
│   CardListFragment.kt      # 首頁清單、拖曳、搜尋
│   CardDetailFragment.kt    # 顯示細節、重新辨識
│   CardEditFragment.kt      # 編輯/新增

app/src/main/res/layout/
│   fragment_card_list.xml
│   fragment_card_detail.xml
│   fragment_card_edit.xml
│   item_card.xml

app/src/main/res/navigation/nav_graph.xml   # 新增 navigation flow
```

---

如需未來擴充功能（多語系、雲端同步、批次掃描等），請參考 `TODO.md`。
