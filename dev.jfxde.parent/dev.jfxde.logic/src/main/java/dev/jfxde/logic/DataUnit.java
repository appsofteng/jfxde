package dev.jfxde.logic;

public enum DataUnit {
	Bit(1), Kibit(1024), Mibit(Kibit.ratio * 1024),
	Byte(8), KiB(Byte.ratio * 1024), MiB(KiB.ratio * 1024), GiB(MiB.ratio * 1024), TiB(GiB.ratio * 1024);

	private double ratio;

	DataUnit(double value) {
		this.ratio = value;
	}

	public double convert(double value, DataUnit unit) {

		return value * unit.ratio / ratio;
	}
}