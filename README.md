# FlowSense


[_FlowSense_ ](https://sites.google.com/umass.edu/flowsense) is available in two versions: Fixed module (Arduino) setup and Smartphone application. 

## Project Directory Tree
- **Codes** 
  - Fixed_module 
    - Arduino_codes : Contains Arduino Nano 33 BLE Sense code for storing FFT features and airflow readings on the SD card.
    - Python_training_codes : Contains training codes and trained models of gradient boosting regression and classification.
  - Smartphone (AndroidStudio & Python)
    - FFTMicForTraining : Contains Android Studio project for data collection app, stores FFT features and airflow readings locally on phone.
    - FlowSense : Contains Android Studio project for app using the deployed FlowSense classification and regression model to predict airflow.
    - train_and_test_FlowSense_smartphone_model.ipynb : Contains training code for gradient boosting regression and classification phone models.
- **Datasets** : 
  - Arduino_dataset: Contains the dataset recorded using arduino microphone and rev.p airflow sensor for different distances, vents and environments.
  - Smartphone : Contains datasets recorded using smartphone app, sorted by different distances and environments, and named by the vent and orientation of the recording.  Also contains "RealWorld" datasets, which include additional distances and data recorded in the presence of ambient noise.
- **Evaluation** : 
  - Privacy Study : 
  - Silence Detection : Contains two notebooks that analyze and detect silent periods in both recorded audio and datasets, and one notebook that generates graphs based on results.
  - Smartphone Evaluation : Contains three notebooks to evaluate the performance of classification and regression models on both our core datasets, and the "RealWorld" datasets; also contains a notebook to generate graphs based on the results.


## How to Run

### Fixed Module

FlowSense has two ML models - regression (to predict rate of airflow) and classification (to predict vent status). To run the Fixed module code, run the [Gradient_boosting_regression.ipynb](https://github.com/umassos/FlowSense/blob/main/Codes/Fixed_module/Python_training_codes/Gradient_boosting_regression.ipynb) or  [Gradient_boosting_classification.ipynb](https://github.com/umassos/FlowSense/blob/main/Codes/Fixed_module/Python_training_codes/Gradient_boosting_classification.ipynb) notebooks. This will train the regressor or classifier model and save it. The trained model for regressor and classifier are already saved as flowSense_regression.joblib and flowSense_classifier.joblib and can directly be used to predict the rate of airflow or vent status given a test sample.


### Smartphone setup

For the smartphone deployment, we have two ML models - regression (to predict rate of airflow) and classification (to predict vent status). To train both of these models, run the [train_and_test_FlowSense_smartphone_model.ipynb](https://github.com/umassos/FlowSense/blob/main/Codes/Smartphone%20(AndroidStudio%20%26%20Python)/train_and_test_FlowSense_smartphone_model.ipynb) notebook. This will train the regressor and classifier models and save them. The trained model for regressor and classifier are already saved as flowSenseReg.joblib and flowSenseClass.joblib and can directly be used to predict the rate of airflow or vent status given a test sample.

The actual deployment of this trained model uses an Android project written in Kotlin, which is in the [Codes/Smartphone/FlowSense](https://github.com/umassos/FlowSense/blob/main/Codes/Smartphone%20(AndroidStudio%20%26%20Python)/FlowSense) folder.  To open and compile this project, use [Android Studio](https://developer.android.com/studio).  

Additionally, note that the .joblib file of our saved model is not directly compatible for Android deployment.  Within the folder [Smartphone/FlowSense/convert_model](https://github.com/umassos/FlowSense/blob/main/Codes/Smartphone%20(AndroidStudio%20%26%20Python)/FlowSense/convert_model), we include two tools (joblib2pmml.py & [JPMML Transpiler](https://github.com/jpmml/jpmml-transpiler)) which are used to convert the model from (.joblib --> PMML --> .jar) for compatibility in the android app.

## Citing this work

FlowSense: Monitoring Airflow in Building Ventilation Systems Using Audio Sensing 

Bhawana Chhaglani, Camellia Zakaria, Adam Lechowicz, Prashant Shenoy, Jeremy Gummeson

ACM IMWUT 2022 
