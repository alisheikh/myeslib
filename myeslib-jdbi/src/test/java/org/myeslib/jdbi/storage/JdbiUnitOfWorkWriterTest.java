package org.myeslib.jdbi.storage;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.core.data.AggregateRootHistory;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.example.SampleDomain.DecreaseInventory;
import org.myeslib.example.SampleDomain.IncreaseInventory;
import org.myeslib.example.SampleDomain.InventoryDecreased;
import org.myeslib.example.SampleDomain.InventoryIncreased;
import org.myeslib.jdbi.AggregateRootHistoryReader;
import org.myeslib.jdbi.AggregateRootHistoryWriter;
import org.myeslib.jdbi.storage.JdbiUnitOfWorkWriter;
import org.skife.jdbi.v2.Handle;

@RunWith(MockitoJUnitRunner.class) 
public class JdbiUnitOfWorkWriterTest {
	
	@Mock
	Handle handle;
	
	@Mock
	AggregateRootHistoryReader<UUID> arReader;
	
	@Mock
	AggregateRootHistoryWriter<UUID> arWriter;
	
	@Test
	public void firstTransactionOnEmptyHistory() {
		
		UUID id = UUID.randomUUID();

		UnitOfWork newUow = UnitOfWork.create(new IncreaseInventory(id, 1), 0l, Arrays.asList(new InventoryIncreased(id, 1)));
		
		when(arReader.get(id, handle)).thenReturn(null);
		
		JdbiUnitOfWorkWriter<UUID> store = new JdbiUnitOfWorkWriter<>(handle, arReader, arWriter);
		
		store.insert(id, newUow);
		
		verify(arReader, times(1)).get(id, handle);
		verify(arWriter, times(1)).insert(id, newUow, handle);
		
	}

	@Test
	public void baseVersionMatchingLastVersion() {
		
		UUID id = UUID.randomUUID();

		UnitOfWork existingUow = UnitOfWork.create(new IncreaseInventory(id, 1), 0l, Arrays.asList(new InventoryIncreased(id, 1)));

		AggregateRootHistory existing = new AggregateRootHistory();

		existing.add(existingUow);
		
		when(arReader.get(id, handle)).thenReturn(existing);
		
		UnitOfWork newUow = UnitOfWork.create(new DecreaseInventory(id, 1), 1L, Arrays.asList(new InventoryDecreased(id, 1)));
		
		JdbiUnitOfWorkWriter<UUID> store = new JdbiUnitOfWorkWriter<>(handle, arReader, arWriter);
		
		store.insert(id, newUow);
		
		verify(arReader, times(1)).get(id, handle);
		verify(arWriter, times(1)).insert(id, newUow, handle);
		
	}
	
	@Test(expected=ConcurrentModificationException.class)
	public void baseVersionDoestNotMatchLastVersion() {
		
		UUID id = UUID.randomUUID();

		UnitOfWork existingUow = UnitOfWork.create(new IncreaseInventory(id, 1), 0l, Arrays.asList(new InventoryIncreased(id, 1)));

		AggregateRootHistory existing = new AggregateRootHistory();

		existing.add(existingUow);
		
		when(arReader.get(id, handle)).thenReturn(existing);

		UnitOfWork newUow = UnitOfWork.create(new DecreaseInventory(id, 1), 0L, Arrays.asList(new InventoryDecreased(id, 1)));
		
		JdbiUnitOfWorkWriter<UUID> store = new JdbiUnitOfWorkWriter<>(handle, arReader, arWriter);
		
		store.insert(id, newUow);
		
		verify(arReader, times(1)).get(id, handle);
		verify(arWriter, times(0)).insert(any(UUID.class), any(UnitOfWork.class), any(Handle.class));
		
	}

	
}

