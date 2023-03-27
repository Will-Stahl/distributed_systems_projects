public interface ConsistencyStrategy
{
    public boolean ServerPublish(String article, int replyTo, BulletinBoardServer selfServer);
    public String ServerRead(BulletinBoardServer selfServer);
    public String ServerChoose(BulletinBoardServer selfServer, int articleID);
}