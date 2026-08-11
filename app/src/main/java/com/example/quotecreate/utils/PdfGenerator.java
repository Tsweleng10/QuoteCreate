package com.example.quotecreate.utils;

import android.content.Context;

import com.example.quotecreate.models.Company;
import com.example.quotecreate.models.LineItem;
import com.example.quotecreate.models.Quote;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    public static File generateQuotePdf(Context context, Quote quote, List<LineItem> items, Company company) {
        try {
            File pdfFile = new File(context.getFilesDir(), "Quote_" + quote.quoteNumber + ".pdf");
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(40, 40, 40, 40);

            // ----- HEADER -----
            // Left side: Company name & tagline
            Paragraph companyName = new Paragraph()
                    .add(new Text("SEAKO").setBold().setFontSize(24).setFontColor(ColorConstants.BLUE))
                    .add(new Text("\n"))
                    .add(new Text("DEVELOPERS PTY LTD").setFontSize(10).setFontColor(ColorConstants.GRAY));
            companyName.setTextAlignment(TextAlignment.LEFT);

            // Right side: Metadata table (2 columns)
            Table metaTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
            metaTable.setWidth(UnitValue.createPercentValue(60));
            // FIXED: Use HorizontalAlignment.RIGHT instead of TextAlignment.RIGHT
            metaTable.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            metaTable.setFontSize(8);
            addMetaRow(metaTable, "COMPANY NAME", company.name != null ? company.name : "SEAKO DEVELOPERS PTY(LTD)");
            addMetaRow(metaTable, "REGISTRATION NO", company.registrationNo != null ? company.registrationNo : "2025/682195/07");
            addMetaRow(metaTable, "TAX NO", company.taxNo != null ? company.taxNo : "9279207279");
            addMetaRow(metaTable, "ADDRESS", company.addressLines != null ? company.addressLines.replace("\n", ", ") : "41 Stiemens street / Johannesburg / 2001");
            addMetaRow(metaTable, "DATE", quote.date != null ? quote.date : new SimpleDateFormat("dd-MMM-yy", Locale.US).format(new java.util.Date()));
            addMetaRow(metaTable, "QUOTE", quote.quoteNumber);
            addMetaRow(metaTable, "REFERENCE", quote.reference != null ? quote.reference : "");

            // Combine header into a 2-column table
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            Cell leftCell = new Cell().add(companyName).setBorder(null);
            Cell rightCell = new Cell().add(metaTable).setBorder(null).setTextAlignment(TextAlignment.RIGHT);
            headerTable.addCell(leftCell);
            headerTable.addCell(rightCell);
            doc.add(headerTable);

            doc.add(new Paragraph("\n"));

            // ----- LINE-ITEM TABLE -----
            float[] colWidths = {4, 40, 8, 4, 10, 4, 16};
            Table itemTable = new Table(UnitValue.createPercentArray(colWidths));
            itemTable.setWidth(UnitValue.createPercentValue(100));

            double total = 0;
            int rowNum = 1;
            for (LineItem item : items) {
                double amount = item.quantity * item.rate;
                total += amount;

                // Row number
                Cell cellNum = new Cell().add(new Paragraph(String.valueOf(rowNum)));
                cellNum.setBorder(null);
                itemTable.addCell(cellNum);

                // Description
                Cell cellDesc = new Cell().add(new Paragraph(item.description != null ? item.description : ""));
                cellDesc.setBorder(null);
                itemTable.addCell(cellDesc);

                // Qty
                String qtyStr = String.format(Locale.US, "%.0f %s", item.quantity, item.unit != null ? item.unit : "hr");
                Cell cellQty = new Cell().add(new Paragraph(qtyStr).setTextAlignment(TextAlignment.RIGHT));
                cellQty.setBorder(null);
                itemTable.addCell(cellQty);

                // Currency symbol for Rate
                Cell cellCurr1 = new Cell().add(new Paragraph("R").setTextAlignment(TextAlignment.RIGHT));
                cellCurr1.setBorder(null);
                itemTable.addCell(cellCurr1);

                // Rate
                String rateStr = String.format(Locale.US, "%.2f", item.rate);
                Cell cellRate = new Cell().add(new Paragraph(rateStr).setTextAlignment(TextAlignment.RIGHT));
                cellRate.setBorder(null);
                itemTable.addCell(cellRate);

                // Currency symbol for Amount
                Cell cellCurr2 = new Cell().add(new Paragraph("R").setTextAlignment(TextAlignment.RIGHT));
                cellCurr2.setBorder(null);
                itemTable.addCell(cellCurr2);

                // Amount
                String amountStr = (amount == 0) ? "-" : String.format(Locale.US, "%,.2f", amount);
                Cell cellAmount = new Cell().add(new Paragraph(amountStr).setTextAlignment(TextAlignment.RIGHT));
                cellAmount.setBorder(null);
                itemTable.addCell(cellAmount);

                rowNum++;
            }

            // Horizontal rule before table
            Paragraph line = new Paragraph().setBorderBottom(new SolidBorder(1));
            doc.add(line);

            doc.add(itemTable);

            // ---- TOTAL ROW ----
            Table totalTable = new Table(UnitValue.createPercentArray(colWidths));
            totalTable.setWidth(UnitValue.createPercentValue(100));
            // First 5 columns merged for label (colspan 5)
            Cell totalLabelCell = new Cell(1, 5)
                    .add(new Paragraph("TOTAL FEES TO PROCUREMENT").setBold().setFontSize(12));
            totalLabelCell.setBorder(null);
            totalTable.addCell(totalLabelCell);

            // Currency symbol for total (6th column)
            Cell totalCurrCell = new Cell().add(new Paragraph("R").setTextAlignment(TextAlignment.RIGHT).setBold());
            totalCurrCell.setBorder(null);
            totalTable.addCell(totalCurrCell);

            // Total amount (7th column)
            String totalStr = String.format(Locale.US, "%,.2f", total);
            Cell totalAmountCell = new Cell().add(new Paragraph(totalStr).setTextAlignment(TextAlignment.RIGHT).setBold());
            totalAmountCell.setBorder(null);
            totalTable.addCell(totalAmountCell);

            doc.add(totalTable);

            // Double horizontal rule
            Paragraph line1 = new Paragraph().setBorderBottom(new SolidBorder(1));
            doc.add(line1);
            Paragraph line2 = new Paragraph().setBorderBottom(new SolidBorder(1));
            doc.add(line2);

            // ----- FOOTER -----
            String disclaimer = quote.disclaimerText != null ? quote.disclaimerText :
                    "This quote covers cost management from initiation to procurement. " +
                            "Construction-phase services (contract admin, site inspections, payment certification, " +
                            "variations, close-out) require a separate scope/fee proposal.";
            Paragraph footer = new Paragraph(disclaimer)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(footer);

            doc.close();
            return pdfFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addMetaRow(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label).setBold());
        labelCell.setBorder(null);
        table.addCell(labelCell);

        Cell valueCell = new Cell().add(new Paragraph(value).setTextAlignment(TextAlignment.RIGHT));
        valueCell.setBorder(null);
        table.addCell(valueCell);
    }
}