from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from joblib import dump,load

reg = load('flowSense.joblib')
pipeline_obj = Pipeline([("model",reg)])

from nyoka import skl_to_pmml

features = ["x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8", "x9"]
skl_to_pmml(pipeline=pipeline_obj,col_names=features,target_name="airflow",pmml_f_name="flowSense.pmml")

exit()
