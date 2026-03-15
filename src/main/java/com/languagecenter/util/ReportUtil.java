package com.languagecenter.util;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReportUtil {

    public static void exportScheduleByClass(Collection<? extends Map<String, ?>> rows, String className, File outFile) throws JRException {
        if (outFile == null) throw new IllegalArgumentException("outFile is null");
        ensureParentDir(outFile);
        Collection<Map<String, ?>> data = (rows != null) ? new ArrayList<>(rows) : Collections.emptyList();

        String jrxml = buildScheduleJrxml();

        JasperReport report = JasperCompileManager.compileReport(
                new ByteArrayInputStream(jrxml.getBytes(StandardCharsets.UTF_8)));

        JRMapCollectionDataSource ds = new JRMapCollectionDataSource(data);

        Map<String,Object> params = new HashMap<>();
        params.put("CLASS_NAME", className != null ? className : "");

        JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, ds);

        JasperExportManager.exportReportToPdfFile(jasperPrint, outFile.getAbsolutePath());
    }

    private static String buildScheduleJrxml(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                +"<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" "
                +"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                +"xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\" "
                +"name=\"schedule\" whenNoDataType=\"AllSectionsNoDetail\" pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"535\" leftMargin=\"30\" rightMargin=\"30\" topMargin=\"20\" bottomMargin=\"20\">"
                +"<parameter name=\"CLASS_NAME\" class=\"java.lang.String\"/>"
                +"<field name=\"date\" class=\"java.lang.String\"/>"
                +"<field name=\"start\" class=\"java.lang.String\"/>"
                +"<field name=\"end\" class=\"java.lang.String\"/>"
                +"<field name=\"room\" class=\"java.lang.String\"/>"

                +"<title><band height=\"50\">"
                +"<textField>"
                +"<reportElement x=\"0\" y=\"0\" width=\"535\" height=\"30\"/>"
                +"<textElement textAlignment=\"Center\">"
                +"<font size=\"16\" isBold=\"true\"/>"
                +"</textElement>"
                +"<textFieldExpression><![CDATA[\"Schedule for \" + $P{CLASS_NAME}]]></textFieldExpression>"
                +"</textField>"
                +"</band></title>"

                +"<columnHeader><band height=\"20\">"

                +"<staticText>"
                +"<reportElement x=\"0\" y=\"0\" width=\"130\" height=\"20\"/>"
                +"<textElement><font isBold=\"true\"/></textElement>"
                +"<text><![CDATA[Date]]></text>"
                +"</staticText>"

                +"<staticText>"
                +"<reportElement x=\"140\" y=\"0\" width=\"120\" height=\"20\"/>"
                +"<textElement><font isBold=\"true\"/></textElement>"
                +"<text><![CDATA[Start]]></text>"
                +"</staticText>"

                +"<staticText>"
                +"<reportElement x=\"270\" y=\"0\" width=\"120\" height=\"20\"/>"
                +"<textElement><font isBold=\"true\"/></textElement>"
                +"<text><![CDATA[End]]></text>"
                +"</staticText>"

                +"<staticText>"
                +"<reportElement x=\"400\" y=\"0\" width=\"135\" height=\"20\"/>"
                +"<textElement><font isBold=\"true\"/></textElement>"
                +"<text><![CDATA[Room]]></text>"
                +"</staticText>"

                +"</band></columnHeader>"

                +"<detail><band height=\"18\">"

                +"<textField>"
                +"<reportElement x=\"0\" y=\"0\" width=\"130\" height=\"18\"/>"
                +"<textFieldExpression><![CDATA[$F{date}]]></textFieldExpression>"
                +"</textField>"

                +"<textField>"
                +"<reportElement x=\"140\" y=\"0\" width=\"120\" height=\"18\"/>"
                +"<textFieldExpression><![CDATA[$F{start}]]></textFieldExpression>"
                +"</textField>"

                +"<textField>"
                +"<reportElement x=\"270\" y=\"0\" width=\"120\" height=\"18\"/>"
                +"<textFieldExpression><![CDATA[$F{end}]]></textFieldExpression>"
                +"</textField>"

                +"<textField>"
                +"<reportElement x=\"400\" y=\"0\" width=\"135\" height=\"18\"/>"
                +"<textFieldExpression><![CDATA[$F{room}]]></textFieldExpression>"
                +"</textField>"

                +"</band></detail>"
                +"</jasperReport>";
    }

    public static void exportScheduleForTeacher(Collection<? extends Map<String, ?>> rows, String teacherName, File outFile) throws JRException {
        if (outFile == null) throw new IllegalArgumentException("outFile is null");
        ensureParentDir(outFile);
        Collection<Map<String, ?>> data = (rows != null) ? new ArrayList<>(rows) : Collections.emptyList();

        String jrxml = buildTeacherScheduleJrxml();

        JasperReport report = JasperCompileManager.compileReport(
                new ByteArrayInputStream(jrxml.getBytes(StandardCharsets.UTF_8)));

        JRMapCollectionDataSource ds = new JRMapCollectionDataSource(data);

        Map<String,Object> params = new HashMap<>();
        params.put("TEACHER_NAME", teacherName != null ? teacherName : "");

        JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, ds);

        JasperExportManager.exportReportToPdfFile(jasperPrint, outFile.getAbsolutePath());
    }

    private static String buildTeacherScheduleJrxml(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                +"<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" "
                +"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                +"xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\" "
                +"name=\"teacher_schedule\" whenNoDataType=\"AllSectionsNoDetail\" pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"535\" leftMargin=\"30\" rightMargin=\"30\" topMargin=\"20\" bottomMargin=\"20\">"

                +"<parameter name=\"TEACHER_NAME\" class=\"java.lang.String\"/>" +"<field name=\"className\" class=\"java.lang.String\"/>" +"<field name=\"date\" class=\"java.lang.String\"/>" +"<field name=\"start\" class=\"java.lang.String\"/>" +"<field name=\"end\" class=\"java.lang.String\"/>" +"<field name=\"room\" class=\"java.lang.String\"/>" +"<field name=\"studentCount\" class=\"java.lang.String\"/>" +"<field name=\"maxStudent\" class=\"java.lang.String\"/>" +"<field name=\"status\" class=\"java.lang.String\"/>" +"<title>" +"<band height=\"50\">" +"<textField>" +"<reportElement x=\"0\" y=\"10\" width=\"535\" height=\"30\"/>" +"<textElement textAlignment=\"Center\">" +"<font size=\"16\" isBold=\"true\"/>" +"</textElement>" +"<textFieldExpression><![CDATA[\"Teacher schedule: \" + $P{TEACHER_NAME}]]></textFieldExpression>" +"</textField>" +"</band>" +"</title>" +"<columnHeader>" +"<band height=\"25\">" +"<staticText><reportElement x=\"0\" y=\"0\" width=\"85\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"11\" isBold=\"true\"/></textElement>" +"<text><![CDATA[Class]]></text></staticText>" +"<staticText><reportElement x=\"85\" y=\"0\" width=\"75\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"11\" isBold=\"true\"/></textElement>" +"<text><![CDATA[Date]]></text></staticText>" +"<staticText><reportElement x=\"160\" y=\"0\" width=\"55\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"11\" isBold=\"true\"/></textElement>" +"<text><![CDATA[Start]]></text></staticText>" +"<staticText><reportElement x=\"215\" y=\"0\" width=\"55\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"11\" isBold=\"true\"/></textElement>" +"<text><![CDATA[End]]></text></staticText>" +"<staticText><reportElement x=\"270\" y=\"0\" width=\"75\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"11\" isBold=\"true\"/></textElement>" +"<text><![CDATA[Room]]></text></staticText>" +"<staticText><reportElement x=\"345\" y=\"0\" width=\"90\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"11\" isBold=\"true\"/></textElement>" +"<text><![CDATA[Students]]></text></staticText>" +"<staticText><reportElement x=\"435\" y=\"0\" width=\"100\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"11\" isBold=\"true\"/></textElement>" +"<text><![CDATA[Status]]></text></staticText>" +"</band>" +"</columnHeader>" +"<detail>" +"<band height=\"22\">" +"<textField>" +"<reportElement x=\"0\" y=\"0\" width=\"85\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"10\"/></textElement>" +"<textFieldExpression><![CDATA[$F{className}]]></textFieldExpression>" +"</textField>" +"<textField>" +"<reportElement x=\"85\" y=\"0\" width=\"75\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"10\"/></textElement>" +"<textFieldExpression><![CDATA[$F{date}]]></textFieldExpression>" +"</textField>" +"<textField>" +"<reportElement x=\"160\" y=\"0\" width=\"55\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"10\"/></textElement>" +"<textFieldExpression><![CDATA[$F{start}]]></textFieldExpression>" +"</textField>" +"<textField>" +"<reportElement x=\"215\" y=\"0\" width=\"55\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"10\"/></textElement>" +"<textFieldExpression><![CDATA[$F{end}]]></textFieldExpression>" +"</textField>" +"<textField>" +"<reportElement x=\"270\" y=\"0\" width=\"75\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"10\"/></textElement>" +"<textFieldExpression><![CDATA[$F{room}]]></textFieldExpression>" +"</textField>" +"<textField>" +"<reportElement x=\"345\" y=\"0\" width=\"90\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"10\"/></textElement>" +"<textFieldExpression><![CDATA[$F{studentCount} + \" / \" + $F{maxStudent}]]></textFieldExpression>" +"</textField>" +"<textField>" +"<reportElement x=\"435\" y=\"0\" width=\"100\" height=\"20\"/>" +"<textElement textAlignment=\"Center\"><font size=\"10\"/></textElement>" +"<textFieldExpression><![CDATA[$F{status}]]></textFieldExpression>" +"</textField>" +"</band>" +"</detail>" +"</jasperReport>";
    }

    private static void ensureParentDir(File outFile) {
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
    }
}
