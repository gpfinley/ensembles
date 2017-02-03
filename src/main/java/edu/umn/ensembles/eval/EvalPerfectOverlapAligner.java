package edu.umn.ensembles.eval;

import edu.umn.ensembles.aligners.AnnotationAligner;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aligner that will generate tuples of annotations only if they overlap in both begin and end
 *
 * Created by gpfinley on 10/21/16.
 */
public class EvalPerfectOverlapAligner extends AnnotationAligner {

    /**
     * // todo: doc. There are nulls in the output of this one
     * @param allAnnotations a list of lists of annotations as provided by MultiCasReader
     * @return
     */
    @Override
    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
        List<List<Annotation>> evalTuples = new ArrayList<>();

        // create a map for each system (first system gold, all others hypotheses)
        List<Map<BeginEnd, Annotation>> beginEndMaps =
                allAnnotations.stream().map(x -> new HashMap<BeginEnd, Annotation>()).collect(Collectors.toList());

        // map all annotations from their begin/end values, for all systems
        for (int i=0; i<allAnnotations.size(); i++) {
            final Map<BeginEnd, Annotation> thisMap = beginEndMaps.get(i);
            allAnnotations.get(i).forEach(a -> thisMap.put(new BeginEnd(a.getBegin(), a.getEnd()), a));
        }

        // build truePositive-cum-falseNegative tuples
        for (Map.Entry<BeginEnd, Annotation> entry : beginEndMaps.get(0).entrySet()) {
            BeginEnd be = entry.getKey();
            List<Annotation> theseAnnotations = new ArrayList<>();
            theseAnnotations.add(entry.getValue());
            for (int i=1; i<beginEndMaps.size(); i++) {
                Annotation thisHypAnnotation = beginEndMaps.get(i).get(be);
                if (thisHypAnnotation == null) {
                    theseAnnotations.add(null);
                } else {
                    theseAnnotations.add(thisHypAnnotation);
                    beginEndMaps.get(i).remove(be);
                }
            }
        }
        // now build false positive entries (will be all null except on the single system where there is a false pos)
        // Requires that non-false-positives have been REMOVED in the prior loop!
        for (int i=1; i<beginEndMaps.size(); i++) {
            for (Annotation falsePosAnnotation : beginEndMaps.get(i).values()) {
                List<Annotation> falsePosTuple = new ArrayList<>();
                for (int j=0; j<allAnnotations.size(); j++) {
                    if (j == i) {
                        falsePosTuple.add(falsePosAnnotation);
                    } else {
                        falsePosTuple.add(null);
                    }
                }
            }
        }
        return evalTuples.iterator();
    }

    // todo: break out into Util since another class uses this
    private static class BeginEnd {
        int begin;
        int end;

        BeginEnd(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BeginEnd beginEnd = (BeginEnd) o;

            if (begin != beginEnd.begin) return false;
            return end == beginEnd.end;

        }

        @Override
        public int hashCode() {
            int result = begin;
            result = 31 * result + end;
            return result;
        }
    }

}
