package pl.baczkowicz.mqttspy.connectivity;

import java.util.Date;

import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.utils.TimeUtils;

public class BaseMqttConnectionState
{
	/** Number of connection attempts made. */
	private int connectionAttempts = 0;

	/** Last connection attempt timestamp. */
	private long lastConnectionAttemptTimestamp = ConnectionUtils.NEVER_STARTED;
	
	/** Last successful connection attempt timestamp. */
	private Date lastSuccessfulConnectionAttempt;	
	
	/** COnnection status. */
	private ConnectionStatus connectionStatus = ConnectionStatus.NOT_CONNECTED;

	/** Disconnection reason (if any). */
	private String disconnectionReason;
	
	private int lastUsedSubscriptionId = 0;

	public BaseMqttConnectionState()
	{
		
	}
	
	// ###
	
	
	/**
	 * Records a connection attempt.
	 */
	protected void recordConnectionAttempt()
	{
		lastConnectionAttemptTimestamp = TimeUtils.getMonotonicTime();
		connectionAttempts++;		
	}
	
	/** Records a successful connection. */
	public void recordSuccessfulConnection()
	{
		lastSuccessfulConnectionAttempt = new Date();
	}
	
	public void incrementLastUsedSubscriptionId()
	{
		lastUsedSubscriptionId++;
	}
	// ###
	
	
	public int getConnectionAttempts()
	{
		return connectionAttempts;
	}

	public void setConnectionAttempts(int connectionAttempts)
	{
		this.connectionAttempts = connectionAttempts;
	}

	public long getLastConnectionAttemptTimestamp()
	{
		return lastConnectionAttemptTimestamp;
	}

	public void setLastConnectionAttemptTimestamp(long lastConnectionAttemptTimestamp)
	{
		this.lastConnectionAttemptTimestamp = lastConnectionAttemptTimestamp;
	}

	public Date getLastSuccessfulConnectionAttempt()
	{
		return lastSuccessfulConnectionAttempt;
	}

	public void setLastSuccessfulConnectionAttempt(Date lastSuccessfulConnectionAttempt)
	{
		this.lastSuccessfulConnectionAttempt = lastSuccessfulConnectionAttempt;
	}

	public ConnectionStatus getConnectionStatus()
	{
		return connectionStatus;
	}

	public void setConnectionStatus(ConnectionStatus connectionStatus)
	{
		this.connectionStatus = connectionStatus;
	}

	public String getDisconnectionReason()
	{
		return disconnectionReason;
	}

	public void setDisconnectionReason(String disconnectionReason)
	{
		this.disconnectionReason = disconnectionReason;
	}

	public int getLastUsedSubscriptionId()
	{
		return lastUsedSubscriptionId;
	}

	public void setLastUsedSubscriptionId(int lastUsedSubscriptionId)
	{
		this.lastUsedSubscriptionId = lastUsedSubscriptionId;
	}
	
	
	
}
