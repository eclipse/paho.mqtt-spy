function publish()
{
    for (i = 0; i < 10; i++)
    {
        mqttspy.publish("mqtt-spy/test2", "hello " + i + ": " + new java.util.Date());
        java.lang.Thread.sleep(1000);
    }
    return true;
}
publish();