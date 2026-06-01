package com.example.businesscardscanner.llm

object PromptBuilder {
    fun buildPrompt(ocrText: String): String = """
        你是一個名片資訊萃取助理。以下是從名片 OCR 辨識出的原始文字，區塊之間以 "---" 分隔：

        $ocrText

        請從上述文字中萃取名片資訊，輸出**純 JSON**，不要任何說明文字或 markdown 格式。
        欄位定義：
        - name: 姓名（人名，非公司名）
        - title: 職稱（如總經理、工程師等）
        - company: 公司或組織名稱
        - phones: 電話號碼陣列（含手機、辦公室、傳真，請標記類型）
        - emails: Email 陣列
        - address: 地址
        - website: 網址
        - wechat: 微信 ID（若有）
        - line: LINE ID（若有）
        - note: 其他無法分類的重要資訊

        若某欄位不存在，填 null 或空陣列。
        phones 格式範例：[{"type":"mobile","number":"0912-345-678"}]

        只輸出 JSON，從 { 開始，到 } 結束。
    """.trimIndent()
}
