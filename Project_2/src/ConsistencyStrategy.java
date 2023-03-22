public interface ConsistencyStrategy
{
    /**
     * Post/Reply to articles
     */
    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer);

    /**
     * based on the used consistency/strategy, this method must determine
     * whether it contacts other servers (quorum) or just uses its local copy
     */
    public String ServerRead(BulletinBoardServer selfServer);
    public String ServerChoose(int articleID, ReferencedTree contentTree);
}