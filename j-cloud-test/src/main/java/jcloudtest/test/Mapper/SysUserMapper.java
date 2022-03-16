package jcloudtest.test.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jcloudtest.test.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.lang.reflect.ParameterizedType;
import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

    private static JavaType getMethodReturnJavaType(Method method) {
        Type type = method.getGenericReturnType();    //判断是否带有泛型
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();//获取泛型类型
            Class rowClass = (Class) ((ParameterizedType) type).getRawType();
            JavaType[] javaTypes = new JavaType[actualTypeArguments.length];
            for (int i = 0; i < actualTypeArguments.length; i++) {            //泛型也可能带有泛型，递归获取
                javaTypes[i] = getJavaType(actualTypeArguments[i]);
            }
            return TypeFactory.defaultInstance().constructParametricType(rowClass, javaTypes);
        } else {        //简单类型直接用该类构建
            JavaTypeClass cla = (Class) type;
            return TypeFactory.defaultInstance().constructParametricType(cla, new JavaType[0]);
        }
    }
}


    List<SysUser> selectAll();