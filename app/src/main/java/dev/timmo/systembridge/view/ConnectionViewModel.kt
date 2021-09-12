package dev.timmo.systembridge.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.timmo.systembridge.data.Connection
import dev.timmo.systembridge.data.ConnectionRepository
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject internal constructor(
    connectionRepository: ConnectionRepository
) : ViewModel() {

    val connections: LiveData<List<Connection>> = liveData {
        emit(connectionRepository.getConnections())
    }
}