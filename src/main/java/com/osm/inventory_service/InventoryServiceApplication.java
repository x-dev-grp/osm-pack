package com.osm.inventory_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.osm.inventory_service",
		"com.xdev",
		"com.xdev.communicator",
		"com.xdev.xdevbase",
		"com.xdev.xdevsecurity"
})
@EnableFeignClients(basePackages = {
		"com.osm.inventory_service",
		"com.xdev",
		"com.xdev.communicator",
		"com.xdev.xdevsecurity"
})
@EnableJpaRepositories(
		basePackages = {
				"com.osm.inventory_service",
				"com.xdev",
				"com.xdev.xdevbase"
		},
		repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class
)
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}
}