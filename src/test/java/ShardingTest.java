import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class ShardingTest {

    @Resource(name = "shardingDataSource")  //此处引入的是配置文件中定义的shardingDataSource
    private DataSource dataSource;

    /**
     * 测试插入数据(id固定)
     * @throws SQLException
     */
    @Test
    public void testInsert() throws SQLException {
        Connection connection = dataSource.getConnection();
        String sql = "insert into t_order(order_id, user_id, status) values(4, 1, 'N')";  //t_order为逻辑表
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
    }

    /**
     * 测试查询(分库分表下)
     * @throws SQLException
     */
    @Test
    public void testQuery() throws SQLException {
        Connection connection = dataSource.getConnection();
        /**
         * select * from t_order
         * 分解sql
         * select * from ds0.t_order_0
         * select * from ds0.t_order_1
         * select * from ds1.t_order_0
         * select * from ds1.t_order_1
         * 最后将结果合并
         */
        String sql = "select * from t_order order by order_id";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            System.out.println("order_id: "+resultSet.getLong("order_id")
                    +", user_id: "+ resultSet.getLong("user_id")
                    + " status: "+resultSet.getString("status"));
        }
        resultSet.close();
        preparedStatement.close();
        connection.close();
    }

    /**
     * 测试批量插入(分布式主键)
     * @throws SQLException
     */
    @Test
    public void testBatchInsert() throws SQLException {
        Connection connection = dataSource.getConnection();
        for (int i = 0; i < 10; i++) {
            String sql = "insert into t_order(user_id, status) values(?, 'N')";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, i);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
        connection.close();
    }
}
