#!/bin/sh
java -jar ./jpmml-transpiler-executable-1.1.12.jar --pmml-input flowSense.pmml --jar-output flowSense.jar --class-name edu.umasslass.healthyair.flowSense
