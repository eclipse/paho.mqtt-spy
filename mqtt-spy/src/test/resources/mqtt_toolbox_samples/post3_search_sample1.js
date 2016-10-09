function search()
{
    if (message.getTopic().contains("test") && message.getPayload().contains("temp"))
    {
        return true;
    }
    return false;
}
search();