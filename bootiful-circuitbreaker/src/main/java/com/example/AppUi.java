package com.example;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.domain.City;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;

import lombok.extern.slf4j.Slf4j;

@SpringUI
@Theme("valo")
@Slf4j
public class AppUi extends UI {
	private static final long serialVersionUID = 1152761617155039730L;

	private final CityClient client;
	private final Grid<City> grid;

	@Autowired
	public AppUi(CityClient client) {
		this.client = client;
		this.grid = new Grid<>(City.class);
	}

	@Override
	protected void init(VaadinRequest request) {
		setContent(grid);
		grid.setWidth(100, Unit.PERCENTAGE);
		grid.setHeight(100, Unit.PERCENTAGE);
		final Collection<City> collection = new ArrayList<>();
		client.getCities().forEach(collection::add);

		log.info("Collected cities : {}", collection.size());

		grid.setItems(collection);
	}
}