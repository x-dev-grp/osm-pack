/*package com.osm.inventory_service.service;

import com.osm.inventory_service.entity.BonCommande;
import com.osm.inventory_service.entity.LigneBonCommande;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] genererBonCommandePdf(BonCommande bc) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);


            PdfFont font = PdfFontFactory.createFont("Helvetica");
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");


            Paragraph titre = new Paragraph("BON DE COMMANDE")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titre);


            Table infoTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

            infoTable.addCell(createCell("N° Bon de commande :", boldFont, true));
            infoTable.addCell(createCell(bc.getNumeroBC(), font, false));

            infoTable.addCell(createCell("Fournisseur :", boldFont, true));
            infoTable.addCell(createCell(bc.getFournisseur() != null ? bc.getFournisseur() : "Non spécifié", font, false));

            infoTable.addCell(createCell("Date création :", boldFont, true));
            infoTable.addCell(createCell(bc.getDateCreation().format(DATE_FORMATTER), font, false));

            infoTable.addCell(createCell("Statut :", boldFont, true));
            infoTable.addCell(createCell(bc.getStatut().toString(), font, false));

            if (bc.getDateValidation() != null) {
                infoTable.addCell(createCell("Date validation :", boldFont, true));
                infoTable.addCell(createCell(bc.getDateValidation().format(DATE_FORMATTER), font, false));
            }

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // TABLEAU DES ARTICLES
            Paragraph sousTitre = new Paragraph("Détail des articles")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(sousTitre);

            Table articleTable = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();

            // En-têtes
            articleTable.addCell(createHeaderCell("Référence", boldFont));
            articleTable.addCell(createHeaderCell("Désignation", boldFont));
            articleTable.addCell(createHeaderCell("Quantité", boldFont));
            articleTable.addCell(createHeaderCell("Prix unitaire (€)", boldFont));

            // Lignes d'articles
            for (LigneBonCommande ligne : bc.getLignes()) {
                articleTable.addCell(createCell(ligne.getArticle().getSku(), font, false));
                articleTable.addCell(createCell(ligne.getArticle().getNom(), font, false));

                String quantite = ligne.getQuantiteCommandee().toString();
                if (ligne.getArticle().getUniteMesure() != null) {
                    quantite += " " + ligne.getArticle().getUniteMesure();
                }
                articleTable.addCell(createCell(quantite, font, false));

                String prix = ligne.getPrixUnitaire() != null ?
                        String.format("%.2f", ligne.getPrixUnitaire()) : "-";
                articleTable.addCell(createCell(prix, font, false));
            }

            document.add(articleTable);

            // PIED DE PAGE
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("Document généré automatiquement par ABIOOC - Système de Gestion de Stock")
                    .setFont(font)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }

    private Cell createCell(String text, PdfFont font, boolean isHeader) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text).setFont(font).setFontSize(isHeader ? 11 : 10));
        cell.setBorder(Border.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }

    private Cell createHeaderCell(String text, PdfFont font) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text).setFont(font).setFontSize(11));
        cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);  // ✅ ColorConstants
        cell.setBorder(new SolidBorder(ColorConstants.BLACK, 1));  // ✅ ColorConstants
        cell.setPadding(8);
        cell.setTextAlignment(TextAlignment.CENTER);
        return cell;
    }
}*/