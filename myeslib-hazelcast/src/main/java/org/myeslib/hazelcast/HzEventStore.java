package org.myeslib.hazelcast;

import java.util.ConcurrentModificationException;

import lombok.AllArgsConstructor;

import org.myeslib.data.AggregateRootHistory;
import org.myeslib.data.UnitOfWork;
import org.myeslib.storage.EventStore;

import com.hazelcast.core.TransactionalMap;

@AllArgsConstructor
public class HzEventStore<K> implements EventStore<K>{

	private final TransactionalMap<K, AggregateRootHistory> pastTransactionsMap ;
	
	public void store(final K id, final UnitOfWork uow) {
		final AggregateRootHistory history = getHistoryFor(id);
		if (history.getLastVersion() != uow.getBaseVersion()){
			throw new ConcurrentModificationException(String.format("version %s does not match the expected %s", 
																	history.getLastVersion().toString(), 
																	uow.getBaseVersion().toString())
													 );
														
		} 
		history.add(uow);
		pastTransactionsMap.set(id, history); // hazelcast optimization --> set instead of put since is void
	}
		
	private AggregateRootHistory getHistoryFor(final K id) {
		AggregateRootHistory pastTransactions = pastTransactionsMap.get(id);
		return pastTransactions == null ? new AggregateRootHistory() : pastTransactions;
	}

}