package jcloudtest.test.Service;

import jcloudtest.test.Mapper.SysUserMapper;
import jcloudtest.test.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class testServiceImpl implements testService {

    @Autowired(required = false)
    private SysUserMapper sysUserMapper;

    @Override
    public List<SysUser> find() {
//        return sysUserMapper.selectAll();
        return null;
    }
}
