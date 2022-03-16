package jcloudtest.test.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jcloudtest.test.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    List<SysUser> selectAll();
}
