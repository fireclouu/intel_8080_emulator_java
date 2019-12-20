
public class CpuComponents
{
	///  PSW FLAG POSITIONS  ///
	public final int 
	PSW_FLAG_POS_CY = 0b_0000_0001, // on bit pos 0 (Carry)
	PSW_FLAG_POS_PA = 0b_0000_0100, // on bit pos 2 (Parity)
	PSW_FLAG_POS_AC = 0b_0001_0000, // on bit pos 4 (Aux. carry)
	PSW_FLAG_POS_ZE = 0b_0100_0000, // on bit pos 6 (Zero)
	PSW_FLAG_POS_SN = 0b_1000_0000; // on bit pos 7 (Sign)

	///  REGISTERS  ///
	public short B, C, D, E, H, L, A;

	///  16-BIT REGISTER ADDRESSES  ///
	public int PC, SP;

	///  MEMORY  ///
	public short memory[];

	///  CONDITIONALS  ///
	public ConditionCodes cc;

	///  ?  ///
	public short int_enable;

	// RESET / INIT
	public CpuComponents(int programLength, short memory[]) {
		B = 0;
		C = 0;
		D = 0;
		E = 0;
		H = 0;
		L = 0;
		A = 0;

		PC = 0;
		SP = 0;

		cc = new ConditionCodes();

		cc.Z = 0;
		cc.S = 0;
		cc.P = 0;
		cc.CY = 0;
		cc.AC = 0;

		this.memory = new short[programLength];
		this.memory = memory;

		// int_enable = 0; ?
		// cc.pad = 0; ?
	}
}

class ConditionCodes
{
	public byte Z, S, P, CY, AC;
	public short pad;
}
