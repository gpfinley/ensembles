package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.Amicus;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.*;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.TypeSystemUtil;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * Created by gpfinley on 10/17/16.
 */
public class XmiWriterAE extends CasAnnotator_ImplBase {

    private final static Semaphore typeSystemWritten = new Semaphore(1);

    private static final Logger LOGGER = Logger.getLogger(XmiWriterAE.class.getName());

    public final static String CONFIG_OUTPUT_DIR = "outputDir";

    @ConfigurationParameter(name = CONFIG_OUTPUT_DIR)
    private String outputDirName;

    private Path outputDir;

    /**
     * Initializes the documentSummaryOutDir.
     *
     * @param context the uima context
     * @throws ResourceInitializationException if we fail to initialize DocumentIdOutputStreamFactory
     */
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        outputDir = Paths.get(outputDirName);
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(CAS cas) throws AnalysisEngineProcessException {
        // todo: test sempahore
        // todo: test to see if this autodetects the TS properly
        if (typeSystemWritten.tryAcquire()) {
            try {
                TypeSystemDescriptionFactory
                        .createTypeSystemDescription()
                        .toXML(Files.newOutputStream(outputDir.resolve("TypeSystem.xml")));
            } catch (IOException | SAXException | ResourceInitializationException e) {
                throw new AnalysisEngineProcessException(e);
            }


//            try {
//                // todo: see if this writes the correct TS, or at least one that is comprehensive
////            CAS mergedView = cas.getView(typeSystemView);
//                CAS mergedView = cas;
//                TypeSystem typeSystem = mergedView.getTypeSystem();
//                TypeSystemDescription typeSystemDescription = TypeSystemUtil.typeSystem2TypeSystemDescription(typeSystem);
//                try {
//                    typeSystemDescription.toXML(Files.newOutputStream(outputDir.resolve("TypeSystem.xml")));
//                } catch (IOException | SAXException e) {
//                    throw new AnalysisEngineProcessException(e);
//                }
//            } catch (CASRuntimeException e) {
//                // todo: change
//                LOGGER.warning(String.format("No view with name %s; not writing type system", typeSystemView));
//            }
        }

        String docID;
        try {
            docID = Util.getDocumentID(cas.getJCas());
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
        Path xmiOutPath = outputDir.resolve(docID + ".xmi");
        try (OutputStream out = new FileOutputStream(xmiOutPath.toFile())) {
            XmiCasSerializer.serialize(cas, out);
        } catch (IOException | SAXException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
