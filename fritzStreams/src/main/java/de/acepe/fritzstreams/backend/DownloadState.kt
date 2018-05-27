package de.acepe.fritzstreams.backend

enum class DownloadState {
    WAITING, DOWNLOADING, FAILED, FINISHED, CANCELLED;

    fun isPending(): Boolean {
        return this === WAITING || this === DOWNLOADING
    }
}