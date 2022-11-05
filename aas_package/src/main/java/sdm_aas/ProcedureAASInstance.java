package sdm_aas;

import java.util.List;
import java.util.ArrayList;

enum AttributeType {
    MATCHING,
    MINIMUM,
    MAXIMUM
}

class ProcessAttribute {
    List<String> semantics;
    String description;
    String stringAttributeValue;
    Double numericAttributeValue;
    List<Object> dimensionalAttributeValue;
    AttributeType attributeType;

    public ProcessAttribute(List<String> semantics, String description, String value) {
        this.semantics = semantics;
        this.description = description;
        this.stringAttributeValue = value;

        this.attributeType = AttributeType.MATCHING;
    }

    public ProcessAttribute(List<String> semantics, String description, Double value, String type) {
        this.semantics = semantics;
        this.description = description;
        this.numericAttributeValue = value;
        if (type == "Minimum") {
            this.attributeType = AttributeType.MINIMUM;
        } else {
            this.attributeType = AttributeType.MAXIMUM;
        }

    }

    public ProcessAttribute(List<String> semantics, String description, List<Object> value, String type) {
        this.semantics = semantics;
        this.description = description;
        this.dimensionalAttributeValue = value;
        if (type == "Minimum") {
            this.attributeType = AttributeType.MINIMUM;
        } else if (type == "Maximum") {
            this.attributeType = AttributeType.MAXIMUM;
        } else {
            this.attributeType = AttributeType.MATCHING;
        }
    }

}

abstract class Process {
    List<ProcessAttribute> processAttributes;
}

class ProcessInstance extends Process {

    public ProcessInstance(List<ProcessAttribute> processAttributes) {
        this.processAttributes = processAttributes;
    }
}

class ProcedureInstance extends Process {
    String resourceURI;
    String processURI;

    public ProcedureInstance(List<ProcessAttribute> processAttributes, String resourceURI) {
        this.processAttributes = processAttributes;
        this.resourceURI = resourceURI;
    }

    public ProcedureInstance(List<ProcessAttribute> processAttributes, String resourceURI, String processURI) {
        this.processAttributes = processAttributes;
        this.resourceURI = resourceURI;
        this.processURI = processURI;
    }

    public void addProcess(String processURI) {
        this.processURI = processURI;
    }
}

public class ProcedureAASInstance {

    public static boolean checkMatchingStringList(List<String> processSemantics, List<String> procedureSemantics) {
        for (String semantic : processSemantics) {
            if (!procedureSemantics.contains(semantic)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkMatchingObjectList(List<Object> processValue, List<Object> procedureValue) {
        for (Object semantic : processValue) {
            if (!procedureValue.contains(semantic)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkMinimumObjectList(List<Object> processValue, List<Object> procedureValue) {
        if (processValue.size() != procedureValue.size()) {
            return false;
        }
        for (int i = 0; i < processValue.size(); i++) {
            if ((Double) processValue.get(i) > (Double) procedureValue.get(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkMaximumObjectList(List<Object> processValue, List<Object> procedureValue) {
        if (processValue.size() != procedureValue.size()) {
            return false;
        }
        for (int i = 0; i < processValue.size(); i++) {
            if ((Double) processValue.get(i) < (Double) procedureValue.get(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkProcedureContainsProcess(Process process, Process procedure) {
        for (ProcessAttribute processAttribute : process.processAttributes) {
            for (ProcessAttribute procedureAttribute : procedure.processAttributes) {
                if (!checkMatchingStringList(processAttribute.semantics, procedureAttribute.semantics)) {
                    return false;
                }
                if (processAttribute.attributeType == AttributeType.MATCHING) {
                    if (processAttribute.stringAttributeValue != null) {
                        return processAttribute.stringAttributeValue == procedureAttribute.stringAttributeValue;
                    }
                    return checkMatchingObjectList(processAttribute.dimensionalAttributeValue,
                            procedureAttribute.dimensionalAttributeValue);

                } else if (processAttribute.attributeType == AttributeType.MINIMUM) {
                    if (processAttribute.numericAttributeValue != null) {
                        return processAttribute.numericAttributeValue <= procedureAttribute.numericAttributeValue;
                    }
                    return checkMinimumObjectList(processAttribute.dimensionalAttributeValue,
                            procedureAttribute.dimensionalAttributeValue);
                } else if (processAttribute.attributeType == AttributeType.MAXIMUM) {
                    if (processAttribute.numericAttributeValue != null) {
                        return processAttribute.numericAttributeValue >= procedureAttribute.numericAttributeValue;
                    }
                    return checkMaximumObjectList(processAttribute.dimensionalAttributeValue,
                            procedureAttribute.dimensionalAttributeValue);
                }
            }
        }

        return false;
    }

    public static List<ProcedureInstance> findValidProcedures(Process process, List<ProcedureInstance> procedures){
        List<ProcedureInstance> validProcedures = new ArrayList<ProcedureInstance>();
        for (ProcedureInstance procedure: procedures){
            if (checkProcedureContainsProcess(process, procedure)){
                validProcedures.add(procedure);
            }
        }
        return validProcedures;
    }

    public static void main(String[] args) {
        List<String> millingSemantics = new ArrayList<String>();
        millingSemantics.add("Milling");

        List<String> millingTechnologySemantics = new ArrayList<String>(millingSemantics);
        millingSemantics.add("Milling Technology");

        List<String> rotationSemantics = new ArrayList<String>(millingSemantics);
        millingSemantics.add("Rotation speed");

        List<String> dimensionSemantics = new ArrayList<String>(millingSemantics);
        millingSemantics.add("Dimensions");

        ProcessAttribute requiredMillingTechnology = new ProcessAttribute(millingTechnologySemantics,
                "Milling technology", "3 Axes");
        ProcessAttribute requiredMillRotationSpeed = new ProcessAttribute(rotationSemantics,
                "Milling roation speed attribute in rpm", 30.0, "Minimum");
        ProcessAttribute requiredDimensions = new ProcessAttribute(dimensionSemantics,
                "Milling dimensions [x, y, z] in mm", List.of(350.0, 50.0, 40.0), "Minimum");

        ProcessInstance millingProcess = new ProcessInstance(
                List.of(requiredMillingTechnology, requiredMillRotationSpeed, requiredDimensions));

        ProcessAttribute actualMillingTechnology3 = new ProcessAttribute(millingTechnologySemantics,
                "Milling technology", "3 Axes");
        ProcessAttribute actualMillingTechnology5 = new ProcessAttribute(millingTechnologySemantics,
                "Milling technology", "5 Axes");
        ProcessAttribute acutalMillRotationSpeed = new ProcessAttribute(rotationSemantics,
                "Milling roation speed attribute in rpm", 12000.0, "Minimum");
        ProcessAttribute actualDimensions = new ProcessAttribute(dimensionSemantics,
                "Milling dimensions [x, y, z] in mm", List.of(600.0, 600.0, 150.0), "Minimum");

        String EXAMPLE_RESOURCE_URI = "http://193.196.37.23:4001/aasServer/shells/ResourceID/aas/";
        ProcedureInstance millingProcedure = new ProcedureInstance(
                List.of(actualMillingTechnology3, actualMillingTechnology5, acutalMillRotationSpeed, actualDimensions),
                EXAMPLE_RESOURCE_URI);

        ProcessAttribute actualMillingTechnology2 = new ProcessAttribute(millingTechnologySemantics,
                "Milling technology", "2 Axes");
        ProcessAttribute acutalMillRotationSpeed2 = new ProcessAttribute(rotationSemantics,
                "Milling roation speed attribute in rpm", 19000.0, "Minimum");
        ProcessAttribute actualDimensions2 = new ProcessAttribute(dimensionSemantics,
                "Milling dimensions [x, y, z] in mm", List.of(1200.0, 1200.0, 150.0), "Minimum");

        String EXAMPLE_RESOURCE_URI2 = "http://193.196.37.23:4001/aasServer/shells/ResourceID2/aas/";
        ProcedureInstance millingProcedure2 = new ProcedureInstance(
                List.of(actualMillingTechnology2, acutalMillRotationSpeed2, actualDimensions2),
                EXAMPLE_RESOURCE_URI2);

        ProcessAttribute acutalMillRotationSpeed3 = new ProcessAttribute(rotationSemantics,
                "Milling roation speed attribute in rpm", 20000.0, "Minimum");
        ProcessAttribute actualDimensions3 = new ProcessAttribute(dimensionSemantics,
                "Milling dimensions [x, y, z] in mm", List.of(15.0, 10.0, 15.0), "Minimum");

        String EXAMPLE_RESOURCE_URI3 = "http://193.196.37.23:4001/aasServer/shells/ResourceID3/aas/";
        ProcedureInstance millingProcedure3 = new ProcedureInstance(
                List.of(actualMillingTechnology3, acutalMillRotationSpeed3, actualDimensions3),
                EXAMPLE_RESOURCE_URI3);

        boolean validProcedure = checkProcedureContainsProcess(millingProcess, millingProcedure);
        System.out.println("Procedure is valid for process: " + validProcedure);


        List<ProcedureInstance> allProcedures = List.of(millingProcedure, millingProcedure2, millingProcedure3);
        List<ProcedureInstance> possibleProcedure = findValidProcedures(millingProcess, allProcedures);
        System.out.println("From " + allProcedures.size() + " are " + possibleProcedure.size() + " possible.");

    }
}
