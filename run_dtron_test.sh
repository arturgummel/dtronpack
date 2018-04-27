#download UPPAAL TRON manually from http://people.cs.aau.dk/~marius/tron/download.html
#You will need to accept the license!
#unzip uppaal-tron-1.5-linux.zip in dtronpack directory:
#cd /catkin_ws/dtronpack && unzip uppaal-tron-1.5-linux.zip

export TRON_HOME=./uppaal-tron-1.5-linux

java -jar dtron-4.14.jar -f generateModelFromTmap/examples/map_model.xml -u 100000 -o 400000 -P eager
