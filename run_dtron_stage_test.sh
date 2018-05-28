# To run dtron:
# 1) unzip uppaal-tron-1.5-linux.zip 

# run spread
#spread -n localhost

# TRON_HOME environment variable pointing to un-zipped TRON distribution downloaded from: http://people.cs.aau.dk/~marius/tron
# 
export TRON_HOME=./uppaal-tron-1.5-linux


java -jar dtron-4.14.jar -f stage_models/stage_output_model_robot_0.xml -u 100000 -o 400000 -P eager &
java -jar dtron-4.14.jar -f stage_models/stage_output_model_robot_1.xml -u 100000 -o 400000 -P eager &
java -jar dtron-4.14.jar -f stage_models/stage_output_model_robot_2.xml -u 100000 -o 400000 -P eager 
