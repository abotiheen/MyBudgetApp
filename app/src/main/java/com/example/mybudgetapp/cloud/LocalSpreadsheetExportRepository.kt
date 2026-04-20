package com.example.mybudgetapp.cloud

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.database.BudgetTransaction
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

interface LocalSpreadsheetExportRepository {
    suspend fun exportSpreadsheet(): Result<String>
}

class ExcelCompatibleSpreadsheetExportRepository(
    private val context: Context,
    private val localDataSource: CloudBackupLocalDataSource,
) : LocalSpreadsheetExportRepository {

    override suspend fun exportSpreadsheet(): Result<String> = runCatching {
        val snapshot = localDataSource.exportSnapshot()
        val fileName = buildFileName(snapshot.exportedAt)
        val payload = buildWorkbookBytes(snapshot)
        saveToDownloads(fileName, payload)
        "Excel workbook saved to Downloads as $fileName."
    }

    private fun buildFileName(exportedAt: String): String {
        val formatted = runCatching {
            val instant = Instant.parse(exportedAt)
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US)
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }.getOrElse {
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US)
                .withZone(ZoneId.systemDefault())
                .format(Instant.now())
        }
        return "mybudget-backup-$formatted.xlsx"
    }

    private fun saveToDownloads(fileName: String, bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(
                    MediaStore.Downloads.MIME_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                )
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create spreadsheet file in Downloads.")
            resolver.openOutputStream(uri)?.use { output ->
                output.write(bytes)
            } ?: error("Unable to open spreadsheet output stream.")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return
        }

        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDirectory.exists()) {
            downloadsDirectory.mkdirs()
        }
        val file = File(downloadsDirectory, fileName)
        file.writeBytes(bytes)
    }

    private fun buildWorkbookBytes(snapshot: BackupSnapshot): ByteArray {
        val sheets = listOf(
            SpreadsheetSheet(
                name = "Overview",
                rows = listOf(
                    listOf(stringCell("Field", header = true), stringCell("Value", header = true)),
                    listOf(stringCell("Exported At"), stringCell(snapshot.exportedAt)),
                    listOf(stringCell("App Version"), numberCell(snapshot.appVersion.toDouble())),
                    listOf(stringCell("Category Count"), numberCell(snapshot.categories.size.toDouble())),
                    listOf(stringCell("Transaction Count"), numberCell(snapshot.transactions.size.toDouble())),
                ),
            ),
            SpreadsheetSheet(
                name = "Categories",
                rows = buildCategoryRows(snapshot.categories),
            ),
            SpreadsheetSheet(
                name = "Transactions",
                rows = buildTransactionRows(snapshot.transactions),
            ),
        )

        return ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                writeZipEntry(zip, "[Content_Types].xml", contentTypesXml(sheets.size))
                writeZipEntry(zip, "_rels/.rels", rootRelsXml())
                writeZipEntry(zip, "docProps/app.xml", appPropsXml(sheets))
                writeZipEntry(zip, "docProps/core.xml", corePropsXml(snapshot.exportedAt))
                writeZipEntry(zip, "xl/workbook.xml", workbookXml(sheets))
                writeZipEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelsXml(sheets.size))
                writeZipEntry(zip, "xl/styles.xml", stylesXml())
                sheets.forEachIndexed { index, sheet ->
                    writeZipEntry(
                        zip,
                        "xl/worksheets/sheet${index + 1}.xml",
                        worksheetXml(sheet.rows),
                    )
                }
            }
            output.toByteArray()
        }
    }

    private fun buildCategoryRows(categories: List<BudgetCategory>): List<List<SpreadsheetCell>> =
        buildList {
            add(
                listOf(
                    stringCell("Category Key", header = true),
                    stringCell("Name", header = true),
                    stringCell("Type", header = true),
                    stringCell("Icon Key", header = true),
                    stringCell("Color Hex", header = true),
                    stringCell("Is Default", header = true),
                    stringCell("Is Archived", header = true),
                    stringCell("Sort Order", header = true),
                )
            )
            categories.forEach { category ->
                add(
                    listOf(
                        stringCell(category.categoryKey),
                        stringCell(category.name),
                        stringCell(category.type),
                        stringCell(category.iconKey),
                        stringCell(category.colorHex),
                        stringCell(category.isDefault.toString()),
                        stringCell(category.isArchived.toString()),
                        numberCell(category.sortOrder.toDouble()),
                    )
                )
            }
        }

    private fun buildTransactionRows(transactions: List<BudgetTransaction>): List<List<SpreadsheetCell>> =
        buildList {
            add(
                listOf(
                    stringCell("Transaction Id", header = true),
                    stringCell("Title", header = true),
                    stringCell("Amount", header = true),
                    stringCell("Category", header = true),
                    stringCell("Type", header = true),
                    stringCell("Transaction Date", header = true),
                    stringCell("Picture Path", header = true),
                )
            )
            transactions.forEach { transaction ->
                add(
                    listOf(
                        numberCell(transaction.transactionId.toDouble()),
                        stringCell(transaction.title.orEmpty()),
                        numberCell(transaction.amount),
                        stringCell(transaction.category),
                        stringCell(transaction.type),
                        stringCell(transaction.transactionDate),
                        stringCell(transaction.picturePath.orEmpty()),
                    )
                )
            }
        }

    private fun writeZipEntry(zip: ZipOutputStream, path: String, contents: String) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(contents.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun contentTypesXml(sheetCount: Int): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
        appendLine("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
        appendLine("""<Default Extension="xml" ContentType="application/xml"/>""")
        appendLine("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
        appendLine("""<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>""")
        appendLine("""<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>""")
        appendLine("""<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>""")
        repeat(sheetCount) { index ->
            appendLine(
                """<Override PartName="/xl/worksheets/sheet${index + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>"""
            )
        }
        appendLine("</Types>")
    }

    private fun rootRelsXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
            <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
            <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
        </Relationships>
    """.trimIndent()

    private fun appPropsXml(sheets: List<SpreadsheetSheet>): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">""")
        appendLine("""<Application>MyBudgetApp</Application>""")
        appendLine("""<DocSecurity>0</DocSecurity>""")
        appendLine("""<ScaleCrop>false</ScaleCrop>""")
        appendLine("""<HeadingPairs><vt:vector size="2" baseType="variant"><vt:variant><vt:lpstr>Worksheets</vt:lpstr></vt:variant><vt:variant><vt:i4>${sheets.size}</vt:i4></vt:variant></vt:vector></HeadingPairs>""")
        appendLine("""<TitlesOfParts><vt:vector size="${sheets.size}" baseType="lpstr">""")
        sheets.forEach { sheet ->
            appendLine("""<vt:lpstr>${escapeXml(sheet.name)}</vt:lpstr>""")
        }
        appendLine("""</vt:vector></TitlesOfParts>""")
        appendLine("""<Company></Company>""")
        appendLine("""<LinksUpToDate>false</LinksUpToDate>""")
        appendLine("""<SharedDoc>false</SharedDoc>""")
        appendLine("""<HyperlinksChanged>false</HyperlinksChanged>""")
        appendLine("""<AppVersion>1.0</AppVersion>""")
        appendLine("""</Properties>""")
    }

    private fun corePropsXml(exportedAt: String): String {
        val timestamp = runCatching { Instant.parse(exportedAt).toString() }.getOrElse { Instant.now().toString() }
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:dcmitype="http://purl.org/dc/dcmitype/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <dc:creator>MyBudgetApp</dc:creator>
                <cp:lastModifiedBy>MyBudgetApp</cp:lastModifiedBy>
                <dcterms:created xsi:type="dcterms:W3CDTF">$timestamp</dcterms:created>
                <dcterms:modified xsi:type="dcterms:W3CDTF">$timestamp</dcterms:modified>
                <dc:title>MyBudget backup export</dc:title>
            </cp:coreProperties>
        """.trimIndent()
    }

    private fun workbookXml(sheets: List<SpreadsheetSheet>): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
        appendLine("""<sheets>""")
        sheets.forEachIndexed { index, sheet ->
            appendLine(
                """<sheet name="${escapeXml(sheet.name.take(31))}" sheetId="${index + 1}" r:id="rId${index + 1}"/>"""
            )
        }
        appendLine("""</sheets>""")
        appendLine("""</workbook>""")
    }

    private fun workbookRelsXml(sheetCount: Int): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        repeat(sheetCount) { index ->
            appendLine(
                """<Relationship Id="rId${index + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet${index + 1}.xml"/>"""
            )
        }
        appendLine(
            """<Relationship Id="rId${sheetCount + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>"""
        )
        appendLine("""</Relationships>""")
    }

    private fun stylesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
            <fonts count="2">
                <font><sz val="11"/><name val="Calibri"/></font>
                <font><b/><sz val="11"/><name val="Calibri"/></font>
            </fonts>
            <fills count="3">
                <fill><patternFill patternType="none"/></fill>
                <fill><patternFill patternType="gray125"/></fill>
                <fill><patternFill patternType="solid"><fgColor rgb="FFE8EEF9"/><bgColor indexed="64"/></patternFill></fill>
            </fills>
            <borders count="1">
                <border><left/><right/><top/><bottom/><diagonal/></border>
            </borders>
            <cellStyleXfs count="1">
                <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
            </cellStyleXfs>
            <cellXfs count="2">
                <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
                <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
            </cellXfs>
            <cellStyles count="1">
                <cellStyle name="Normal" xfId="0" builtinId="0"/>
            </cellStyles>
        </styleSheet>
    """.trimIndent()

    private fun worksheetXml(rows: List<List<SpreadsheetCell>>): String = buildString {
        val maxColumnCount = rows.maxOfOrNull { it.size } ?: 1
        val lastCell = "${columnName(maxColumnCount)}${rows.size.coerceAtLeast(1)}"
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        appendLine("""<dimension ref="A1:$lastCell"/>""")
        appendLine("""<sheetData>""")
        rows.forEachIndexed { rowIndex, row ->
            appendLine("""<row r="${rowIndex + 1}">""")
            row.forEachIndexed { columnIndex, cell ->
                appendLine(cellXml(columnIndex + 1, rowIndex + 1, cell))
            }
            appendLine("""</row>""")
        }
        appendLine("""</sheetData>""")
        appendLine("""</worksheet>""")
    }

    private fun cellXml(columnIndex: Int, rowIndex: Int, cell: SpreadsheetCell): String {
        val reference = "${columnName(columnIndex)}$rowIndex"
        val style = if (cell.header) """ s="1"""" else ""
        return when (cell.type) {
            SpreadsheetCellType.NUMBER ->
                """<c r="$reference"$style><v>${cell.value}</v></c>"""
            SpreadsheetCellType.STRING ->
                """<c r="$reference"$style t="inlineStr"><is><t xml:space="preserve">${escapeXml(cell.value)}</t></is></c>"""
        }
    }

    private fun columnName(index: Int): String {
        var current = index
        val builder = StringBuilder()
        while (current > 0) {
            val remainder = (current - 1) % 26
            builder.append(('A'.code + remainder).toChar())
            current = (current - 1) / 26
        }
        return builder.reverse().toString()
    }

    private fun stringCell(value: String, header: Boolean = false): SpreadsheetCell =
        SpreadsheetCell(type = SpreadsheetCellType.STRING, value = value, header = header)

    private fun numberCell(value: Double, header: Boolean = false): SpreadsheetCell =
        SpreadsheetCell(type = SpreadsheetCellType.NUMBER, value = value.toString(), header = header)

    private fun escapeXml(value: String): String = buildString(value.length) {
        value.forEach { character ->
            append(
                when (character) {
                    '&' -> "&amp;"
                    '<' -> "&lt;"
                    '>' -> "&gt;"
                    '"' -> "&quot;"
                    '\'' -> "&apos;"
                    else -> character
                }
            )
        }
    }
}

private data class SpreadsheetSheet(
    val name: String,
    val rows: List<List<SpreadsheetCell>>,
)

private data class SpreadsheetCell(
    val type: SpreadsheetCellType,
    val value: String,
    val header: Boolean = false,
)

private enum class SpreadsheetCellType {
    STRING,
    NUMBER,
}
