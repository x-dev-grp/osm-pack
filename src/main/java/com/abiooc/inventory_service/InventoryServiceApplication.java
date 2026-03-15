package com.abiooc.inventory_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.xdev", "com.xdev.communicator", "com.xdev.xdevsecurity", "com.osm.inventory-service"})
@ComponentScan(basePackages = {"com.xdev", "com.xdev.xdevbase", "com.osm.inventory-service"})
@EnableJpaRepositories(basePackages = {"com.xdev", "com.xdev.xdevbase", "com.osm.inventory-service"}, repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)

public class InventoryServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

}