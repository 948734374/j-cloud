package jcloudtest.test.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class SysUser {

    @TableId(value = "id")
    private int id;
    @TableField(value = "name")
    private String name;
    @TableField(value = "password")
    private String password;
}
