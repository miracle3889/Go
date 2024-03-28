innodb和myisam区别：
    innodb支持事务，有行锁。

存储结构：
    页（16k）.
页分裂：
    插入页满了，就要申请新页，在进行重分配

事物ACID
A 原子性
    一个事物操作要么都执行，要么都不执行
C 一致性

I 隔离性
    隔离级别：
        读未提交       有脏读问题
        读已提交       有不可重复读问题，oracle的默认隔离级别
        重复读         mysql的默认级别
        串行           事务之间互斥，必须一个个执行。由锁实现。
    RC和RR由MVCC机制实现。
    undolog实现原子性用于rollback，另外undolog构成版本链用来实现MVCC机制。
    快照读ReadView结构由:    
        创建ReadView时的活跃事务（未提交事务）数组m_ids[]
        未提交事务最小事务id记录  min_trx_id
        下一个可分配事物id       max_trx_id
        当前事物id              cur_trx_id
    RR级别ReadView创建后不会变，可以保证可重复度
    RC级别每次select新创建ReadView
D 持久性
    redolog保证
    redolog二阶段提交，redo-prepare=》binlog-》redo-commit
    如果写入binlog后宕机，redo日志为prepare也可以照常恢复

MVCC的作用：
    实现了RC和RR级别。解决快照读下的幻读问题。也是一种乐观锁的方式，保证读和写是可以并发的。
锁的作用：
    当前读的幻读问题，通过锁的互斥操作；写的并发问题。

覆盖索引：
    要查询的列在索引中都存在，不需要回表

索引下推(ICP)：
    是指联合索引中例如（a,b），例如  a like 'Xu%' and b = 'xxx'。只用到a,没有ICP时，把所有id找出来，然后回表所有数据，对b进行过滤
    ICP的情况下，根据a的所有数据，先对b进行过滤，查到所有id，然后回表拿到数据。
    优点:减少回表次数，提前筛选过滤。
    

锁：
    读读之间不互斥、读写互斥
    读锁：
        select .. lock in share mode
    写锁：
        select .. for update
    insert、update、delete都会加写锁

快照读：
    ReadView，所有普通的select都是快照读
当前读：
    lock in share mode，for update，普通的update、insert、delete都是当前读
RR级别下的幻读问题：
    快照读通过MVCC解决幻读问题，当前读通过临建锁解决，如果事物中既有快照读又有当前读，还是有幻读问题。

锁：
    行锁、表锁；共享、排他
行锁：
    分为：记录锁（Rec_not_gap）,例如 id = 10 、间隙锁（Gap）：锁住一个开区间 例如 id < 10 and id > 1;
            临键锁(next_key)j就是记录锁+gap锁，例如 id <=10
        另外两种不重要的行锁有：插入意向锁，当有事物锁住一个范围，另一个事物要在范围内插入数据时，会分配一个插入意向锁，等gap锁释放就可以插入了。
                            隐式锁：普通的insert不带锁，
explain

