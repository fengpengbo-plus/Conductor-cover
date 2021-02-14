package com.fengpb.conductor.dao;

import com.alibaba.fastjson.JSONObject;
import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.metadata.tasks.TaskDef;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.common.run.Workflow;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes({TaskDef.class, WorkflowDef.class, Workflow.class, Task.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonTypeHandler<T extends Object> extends BaseTypeHandler<T> {

    private Class<T> clazz;

    public JsonTypeHandler(Class<T> clazz) {
        if (clazz == null) throw new IllegalArgumentException("Type argument cannot be null");
        this.clazz = clazz;
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, T t, JdbcType jdbcType) throws SQLException {

    }

    @Override
    public T getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return this.toObject(resultSet.getString(s), clazz);
    }

    @Override
    public T getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return this.toObject(resultSet.getString(i), clazz);
    }

    @Override
    public T getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return this.toObject(callableStatement.getString(i), clazz);
    }

    private T toObject(String content, Class<?> clazz) {
        if (StringUtils.isNotBlank(content)) {
            try {
                return (T) JSONObject.parseObject(content, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }
}
