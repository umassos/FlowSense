package edu.umasslass.healthyair.fftmic.source;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.*;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.TargetField;
import org.jpmml.evaluator.ModelEvaluatorBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class to handle using JPMMLEvaluator for predictions using the python-trained scikit-learn model on phone
 *
 * @author Adam Lechowicz 08/2021
 */

public class Regressor {

    private Evaluator evaluator;
    private int frequency = 1000;
    private int sampleSize = 16;
    private int numSamples = 0;
    private List listSamples = new ArrayList();
    private String meanValue = "0.0000";

    public void init(){
        evaluator = new ModelEvaluatorBuilder(new edu.umasslass.healthyair.flowSense()).build();
    }

    public String onFFT(float[] fft){
        String targetValue = "";

        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

        List<? extends InputField> inputFields = evaluator.getInputFields();
        int i = 0;
        for(InputField inputField : inputFields){
            FieldName inputName = inputField.getName();

            Object rawValue = Math.sqrt(Math.pow(fft[i], 2.0) + Math.pow(fft[i + 1], 2.0));
            i += 2;

            // Transforming an arbitrary user-supplied value to a known-good PMML value
            // The user-supplied value is passed through: 1) outlier treatment, 2) missing value treatment, 3) invalid value treatment and 4) type conversion
            FieldValue inputValue = inputField.prepare(rawValue);

            arguments.put(inputName, inputValue);
        }
        Map<FieldName, ?> results = evaluator.evaluate(arguments);

        List<? extends TargetField> targetFields = evaluator.getTargetFields();
        for(TargetField targetField : targetFields){
            FieldName targetName = targetField.getName();

            targetValue = results.get(targetName).toString();
        }

        numSamples++;
        listSamples.add(Double.parseDouble(targetValue));

        if(numSamples == 15){
            meanValue = Double.toString(listSamples.stream()
                    .mapToDouble(d -> (double) d)
                    .average()
                    .orElse(0.0)).substring(0,7);
            numSamples = 0;
            listSamples.clear();
        }

        return meanValue;
    }
}
