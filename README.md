# FlowSense


[_FlowSense_ ](https://sites.google.com/umass.edu/flowsense) is available in two versions: Fixed module (Arduino) setup and Smartphone application. 

## Project Directory Tree
- **Codes** 
  - Fixed_module 
    - Arduino_codes : Contains Arduino Nano 33 BLE Sense code for storing FFT features and airflow readings on the SD card.
    - Python_training_codes : Contains training codes and trained models of gradient boosting regression and classification.
- **Datasets** : 
  - Arduino_dataset: Contains the dataset recorded using ardunio microphone and rev.p airflow sensor for different distances, vents and environments.
- **Evaluation** : 


## How to Run

### Fixed Module

FlowSense has two ML models - regression (to predict rate of airflow) and classification (to predict vent status). To run the Fixed module code, run the [Gradient_boosting_regression.ipynb](https://github.com/umassos/FlowSense/blob/main/Codes/Fixed_module/Python_training_codes/Gradient_boosting_regression.ipynb) or  [Gradient_boosting_classification.ipynb](https://github.com/umassos/FlowSense/blob/main/Codes/Fixed_module/Python_training_codes/Gradient_boosting_classification.ipynb) notebooks. This will train the regressor or classifier model and save it. The trained model for regressor and classifier are already saved as flowSense_regression.joblib and flowSense_classifier.joblib and can directly be used to predict the rate of airflow or vent status given a test sample.


### Smartphone setup

## Citing this work

FlowSense: Monitoring Airflow in Building Ventilation Systems Using Audio Sensing 

Bhawana Chhaglani, Camellia Zakaria, Adam Lechowicz, Prashant Shenoy, Jeremy Gummeson

ACM IMWUT 2022 
