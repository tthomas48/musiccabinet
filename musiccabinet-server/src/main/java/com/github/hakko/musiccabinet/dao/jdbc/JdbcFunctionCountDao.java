package com.github.hakko.musiccabinet.dao.jdbc;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.FunctionCountDao;

public class JdbcFunctionCountDao implements FunctionCountDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
        
        private final String COUNT_FUNCTIONS_BY_NAME="select util.count_functions(?)";

	@Override
	public int countFunctions() {
		return jdbcTemplate.queryForInt("select util.count_functions()");
	}

	@Override
	public int countFunctionsByName(String name) {
		return jdbcTemplate.queryForInt(COUNT_FUNCTIONS_BY_NAME,new Object[]{name});
	}
	
	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	// Spring setters
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

}