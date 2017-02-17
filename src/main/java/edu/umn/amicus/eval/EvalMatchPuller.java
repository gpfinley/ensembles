package edu.umn.amicus.eval;

import edu.umn.amicus.EvalAnnotation;
import edu.umn.amicus.pullers.AnnotationPuller;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Used with a Collector to generate summary data for eval matches.
 *
 * Created by greg on 2/10/17.
 */
public class EvalMatchPuller extends AnnotationPuller<EvalMatch> {

    public EvalMatchPuller(String fieldName) {
        super(fieldName);
    }

    @Override
    public EvalMatch transform(Annotation annotation) {
        EvalAnnotation ea = (EvalAnnotation) annotation;
        return new EvalMatch(ea.getSystemIndex(), ea.getStatus(), ea.getScore());
    }

}