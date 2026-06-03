package top.chancelethay.bingo.lib.api;

public interface ExtensionTask {
	boolean isCancelled();
	boolean isSync();

	void cancel();
}
