// Wrap the script in a method, so that you can do "return false;" in case of an error or stop request
function publish()
{
        mqttspy.publish("mqtt-spy-daemon/test1", "hello");
        
        mqttspy.publish("mqtt-spy-daemon/test2", "from");
        
        mqttspy.publish("mqtt-spy-daemon/test3", "mqtt-spy-daemon!");     

        // This means all OK, script has completed without any issues and as expected
        return true;
}

publish();