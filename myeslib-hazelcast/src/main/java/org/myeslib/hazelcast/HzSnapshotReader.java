package org.myeslib.hazelcast;

import static org.myeslib.util.EventSourcingMagicHelper.applyEventsOn;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.data.AggregateRootHistory;
import org.myeslib.data.Snapshot;
import org.myeslib.storage.SnapshotReader;

import com.google.common.base.Function;

@AllArgsConstructor
public class HzSnapshotReader<K, A extends AggregateRoot> implements SnapshotReader<K, A> {
    
	private final Map<K, String> eventsMap ;
	private final Map<K, Snapshot<A>> lastSnapshotMap ; 
	private final Function<String, AggregateRootHistory> fromStringFunction ;
	
	public Snapshot<A> get(final K id, final A aggregateRootFreshInstance) {
		final AggregateRootHistory transactionHistory = getEventsOrEmptyIfNull(id);
		final Long lastVersion = transactionHistory.getLastVersion();
		final Snapshot<A> lastSnapshot = lastSnapshotMap.get(id);
		final Snapshot<A> resultingSnapshot;
		if (lastSnapshot==null){
			resultingSnapshot = applyAllEventsOnFreshInstance(transactionHistory, aggregateRootFreshInstance);
		} else {
			if (lastSnapshot.getVersion() < lastVersion) {
				resultingSnapshot = applyEventsSinceLastSnapshot(transactionHistory, lastSnapshot);
			} else {
				resultingSnapshot = lastSnapshot;
			}
		}
		return resultingSnapshot;
	}

	private AggregateRootHistory getEventsOrEmptyIfNull(final K id) {
		String asString = eventsMap.get(id);
		final AggregateRootHistory events = fromStringFunction.apply(asString);
		return events == null ? new AggregateRootHistory() : events;
	}
	
	private Snapshot<A> applyAllEventsOnFreshInstance(final AggregateRootHistory transactionHistory, 
													  final A aggregateRootFreshInstance) {
		final Long lastVersion = transactionHistory.getLastVersion();
		final List<? extends Event> eventsToApply = transactionHistory.getEventsUntil(lastVersion);
		applyEventsOn(eventsToApply, aggregateRootFreshInstance);
		return new Snapshot<A>(aggregateRootFreshInstance, lastVersion);
	}
	
	private Snapshot<A> applyEventsSinceLastSnapshot(final AggregateRootHistory transactionHistory,
													 final Snapshot<A> lastSnapshot) {
		final Long lastVersion = transactionHistory.getLastVersion();
		final List<? extends Event> eventsToApply = transactionHistory.getEventsAfterUntil(lastSnapshot.getVersion(), lastVersion);
		applyEventsOn(eventsToApply, lastSnapshot.getAggregateInstance());
		return new Snapshot<A>(lastSnapshot.getAggregateInstance(), lastVersion);
	}

}
