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
