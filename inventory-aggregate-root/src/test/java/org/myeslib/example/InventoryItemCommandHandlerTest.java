package org.myeslib.example;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.core.Event;
import org.myeslib.example.SampleDomain.CreateCommandHandler;
import org.myeslib.example.SampleDomain.CreateInventoryItem;
import org.myeslib.example.SampleDomain.DecreaseCommandHandler;
import org.myeslib.example.SampleDomain.DecreaseInventory;
import org.myeslib.example.SampleDomain.IncreaseCommandHandler;
import org.myeslib.example.SampleDomain.IncreaseInventory;
import org.myeslib.example.SampleDomain.InventoryDecreased;
import org.myeslib.example.SampleDomain.InventoryIncreased;
import org.myeslib.example.SampleDomain.InventoryItemAggregateRoot;
import org.myeslib.example.SampleDomain.InventoryItemCreated;
import org.myeslib.example.SampleDomain.ItemDescriptionGeneratorService;

@RunWith(MockitoJUnitRunner.class)
public class InventoryItemCommandHandlerTest {

	@Mock
	ItemDescriptionGeneratorService uuidGeneratorService ;

	@Test(expected=NullPointerException.class)
	public void createWithNullService() {
		
		InventoryItemAggregateRoot aggregateRoot = new InventoryItemAggregateRoot();
		
		CreateInventoryItem command = new CreateInventoryItem(UUID.randomUUID(), UUID.randomUUID());
		
		CreateCommandHandler commandHandler = new CreateCommandHandler(aggregateRoot, null);
		
		commandHandler.handle(command);
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void createOnAnExistingInstance() {
		
		InventoryItemAggregateRoot aggregateRoot = new InventoryItemAggregateRoot();
		
		UUID id = UUID.randomUUID();
		
		aggregateRoot.setId(id);
		
		CreateInventoryItem command = new CreateInventoryItem(UUID.randomUUID(), id);
		
		CreateCommandHandler commandHandler = new CreateCommandHandler(aggregateRoot, uuidGeneratorService);
	
		commandHandler.handle(command);
	
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void increaseOnAnWrongInstance() {
		
		InventoryItemAggregateRoot aggregateRoot = new InventoryItemAggregateRoot();
		
		UUID id = UUID.randomUUID();
		
		aggregateRoot.setId(id);
		
		IncreaseInventory command = new IncreaseInventory(UUID.randomUUID(), UUID.randomUUID(), 1, 0L);
		
		IncreaseCommandHandler commandHandler = new IncreaseCommandHandler(aggregateRoot);
	
		commandHandler.handle(command);
	
	}
	
	@Test
	public void createWithValidService() {
		
		InventoryItemAggregateRoot aggregateRoot = new InventoryItemAggregateRoot();
		
		UUID id = UUID.randomUUID();
		String desc = "item1";
		
		CreateInventoryItem command = new CreateInventoryItem(UUID.randomUUID(), id);
		
		CreateCommandHandler commandHandler = new CreateCommandHandler(aggregateRoot, uuidGeneratorService);
	
		when(uuidGeneratorService.generate(id)).thenReturn(desc);
		
		List<? extends Event> events = commandHandler.handle(command);
	
		verify(uuidGeneratorService).generate(id);
		
		Event expectedEvent = new InventoryItemCreated(id, desc);
		
		assertThat(events.get(0), equalTo(expectedEvent));
		
	}
	
	
	@Test
	public void increase() {
		
		InventoryItemAggregateRoot aggregateRoot = new InventoryItemAggregateRoot();
		
		UUID id = UUID.randomUUID();
		String desc = "item1";
		
		aggregateRoot.setAvailable(0);
		aggregateRoot.setDescription(desc);
		aggregateRoot.setId(id);
		
		IncreaseInventory command = new IncreaseInventory(UUID.randomUUID(), id, 3, 0L);
		
		IncreaseCommandHandler commandHandler = new IncreaseCommandHandler(aggregateRoot);
	
		List<? extends Event> events = commandHandler.handle(command);
	
		Event expectedEvent = new InventoryIncreased(id, 3);
		
		assertThat(events.get(0), is(expectedEvent));
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void decreaseNotAvaliable() {
		
		InventoryItemAggregateRoot aggregateRoot = new InventoryItemAggregateRoot();
		
		UUID id = UUID.randomUUID();
		String desc = "item1";
		
		aggregateRoot.setAvailable(2);
		aggregateRoot.setDescription(desc);
		aggregateRoot.setId(id);
		
		DecreaseInventory command = new DecreaseInventory(UUID.randomUUID(), id, 3, 0L);
		
		DecreaseCommandHandler commandHandler = new DecreaseCommandHandler(aggregateRoot);
	
		commandHandler.handle(command);
		
	}
	
	@Test
	public void decrease() {
		
		InventoryItemAggregateRoot aggregateRoot = new InventoryItemAggregateRoot();
		
		UUID id = UUID.randomUUID();
		String desc = "item1";
		
		aggregateRoot.setAvailable(4);
		aggregateRoot.setDescription(desc);
		aggregateRoot.setId(id);
		
		DecreaseInventory command = new DecreaseInventory(UUID.randomUUID(), id, 3, 0L);
		
		DecreaseCommandHandler commandHandler = new DecreaseCommandHandler(aggregateRoot);
	
		List<? extends Event> events = commandHandler.handle(command);
	
		Event expectedEvent = new InventoryDecreased(id, 3);
		
		assertThat(events.get(0), equalTo(expectedEvent));
		
	}
	
}
