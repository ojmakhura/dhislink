package bw.ub.ehealth;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class LabReport {

    Logger logger = LoggerFactory.getLogger(LabReport.class);

    public LabReport() {

    }

    public JasperReport compileReport(String templateName) {
        try {
            InputStream stream = LabReport.class.getResourceAsStream("/reports/" + templateName + ".jrxml");
            JasperReport report = JasperCompileManager.compileReport(stream);
            JRSaver.saveObject(report, templateName + ".jasper");

            return report;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public byte[] createReport(JasperReport report, Object bean, String reportName) {
        try {
            List list = new ArrayList<>();
            list.add(bean);
            JasperPrint print = JasperFillManager.fillReport(report, null, new JRBeanCollectionDataSource(new ArrayList(list)));

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(reportName));

            SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
            reportConfig.setSizePageToContent(true);
            reportConfig.setForceLineBreakPolicy(false);

            SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
            exporter.getExporterOutput().getOutputStream();
            exporter.setConfiguration(exportConfig);
            
            byte[] data = JasperExportManager.exportReportToPdf(print);
            return data;
            //exporter.exportReport();

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
