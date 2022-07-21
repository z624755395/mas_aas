package multiAgentSystem;

public class MAS {
	private ProductAgent pa;
	private ResourceAgent1 ra;
	public MAS(ProductAgent p, ResourceAgent1 r) {
		pa = p;
		ra = r;
	}
	public ProductAgent getPa() {
		return pa;
	}
	public ResourceAgent1 getRa() {
		return ra;
	}

}
