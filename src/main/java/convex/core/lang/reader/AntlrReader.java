package convex.core.lang.reader;

import java.util.ArrayList;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import convex.core.data.ACell;
import convex.core.data.AHashMap;
import convex.core.data.Address;
import convex.core.data.Blob;
import convex.core.data.Keyword;
import convex.core.data.Lists;
import convex.core.data.Maps;
import convex.core.data.Sets;
import convex.core.data.Symbol;
import convex.core.data.Syntax;
import convex.core.data.Vectors;
import convex.core.data.prim.CVMBool;
import convex.core.data.prim.CVMChar;
import convex.core.data.prim.CVMLong;
import convex.core.exceptions.ParseException;
import convex.core.lang.reader.antlr.ConvexLexer;
import convex.core.lang.reader.antlr.ConvexListener;
import convex.core.lang.reader.antlr.ConvexParser;
import convex.core.lang.reader.antlr.ConvexParser.AddressContext;
import convex.core.lang.reader.antlr.ConvexParser.BlobContext;
import convex.core.lang.reader.antlr.ConvexParser.BoolContext;
import convex.core.lang.reader.antlr.ConvexParser.CharacterContext;
import convex.core.lang.reader.antlr.ConvexParser.DataStructureContext;
import convex.core.lang.reader.antlr.ConvexParser.FormContext;
import convex.core.lang.reader.antlr.ConvexParser.FormsContext;
import convex.core.lang.reader.antlr.ConvexParser.KeywordContext;
import convex.core.lang.reader.antlr.ConvexParser.ListContext;
import convex.core.lang.reader.antlr.ConvexParser.LiteralContext;
import convex.core.lang.reader.antlr.ConvexParser.LongValueContext;
import convex.core.lang.reader.antlr.ConvexParser.MapContext;
import convex.core.lang.reader.antlr.ConvexParser.NilContext;
import convex.core.lang.reader.antlr.ConvexParser.QuotedContext;
import convex.core.lang.reader.antlr.ConvexParser.SetContext;
import convex.core.lang.reader.antlr.ConvexParser.SymbolContext;
import convex.core.lang.reader.antlr.ConvexParser.SyntaxContext;
import convex.core.lang.reader.antlr.ConvexParser.VectorContext;

public class AntlrReader {
	
	public static class CRListener implements ConvexListener {
		ArrayList<ArrayList<ACell>> stack=new ArrayList<>();
		
		public CRListener() {
			stack.add(new ArrayList<>());
		}
		
		public void push(ACell a) {
			int n=stack.size()-1;
			ArrayList<ACell> top=stack.get(n);
			top.add(a);
		}
		
		public ACell pop() {
			int n=stack.size()-1;
			ArrayList<ACell> top=stack.get(n);
			int c=top.size()-1;
			ACell cell=top.get(c);
			top.remove(c);
			return cell;
		}

		
		private void pushList() {
			stack.add(new ArrayList<>());
		}
		
		public ArrayList<ACell> popList() {
			int n=stack.size()-1;
			ArrayList<ACell> top=stack.get(n);
			stack.remove(n);
			return top;
		}

		@Override
		public void visitTerminal(TerminalNode node) {
			// Nothing to do
		}

		@Override
		public void visitErrorNode(ErrorNode node) {
			throw new ParseException(node.toString());
		}

		@Override
		public void enterEveryRule(ParserRuleContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitEveryRule(ParserRuleContext ctx) {
			// Nothing to do
		}

		@Override
		public void enterForm(FormContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitForm(FormContext ctx) {
			// Nothing to do
		}

		@Override
		public void enterForms(FormsContext ctx) {
			// We add a new ArrayList to the stack to capture values
			pushList();
		}

		@Override
		public void exitForms(FormsContext ctx) {
			// Nothing to do
		}

		@Override
		public void enterDataStructure(DataStructureContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitDataStructure(DataStructureContext ctx) {
			// Nothing to do
		}

		@Override
		public void enterList(ListContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitList(ListContext ctx) {
			ArrayList<ACell> elements=popList();
			push(Lists.create(elements));
		}

		@Override
		public void enterVector(VectorContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitVector(VectorContext ctx) {
			ArrayList<ACell> elements=popList();
			push(Vectors.create(elements));
		}

		@Override
		public void enterSet(SetContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitSet(SetContext ctx) {
			ArrayList<ACell> elements=popList();
			push(Sets.fromCollection(elements));
		}
		
		@Override
		public void enterMap(MapContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitMap(MapContext ctx) {
			ArrayList<ACell> elements=popList();
			push(Maps.create(elements.toArray(new ACell[elements.size()])));
		}

		@Override
		public void enterLiteral(LiteralContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitLiteral(LiteralContext ctx) {
			// Nothing to do
		}

		@Override
		public void enterLongValue(LongValueContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitLongValue(LongValueContext ctx) {
			String s=ctx.getText();
			// System.out.println(s);
			push( CVMLong.parse(s));
		}

		@Override
		public void enterNil(NilContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitNil(NilContext ctx) {
			push(null);
		}

		@Override
		public void enterBool(BoolContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitBool(BoolContext ctx) {
			push(CVMBool.parse(ctx.getText()));
		}

		@Override
		public void enterCharacter(CharacterContext ctx) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void exitCharacter(CharacterContext ctx) {
			String s=ctx.getText();
			CVMChar c=CVMChar.parse(s);
			if (c==null) throw new ParseException("Bad chracter literal format: "+s);
			push(c);
		}

		@Override
		public void enterKeyword(KeywordContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitKeyword(KeywordContext ctx) {
			String s=ctx.getText();
			Keyword k=Keyword.create(s.substring(1));
			if (k==null) throw new ParseException("Bad keyword format: "+s);
			push( k);
		}

		@Override
		public void enterSymbol(SymbolContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitSymbol(SymbolContext ctx) {
			String s=ctx.getText();
			Symbol sym=Symbol.create(s);
			if (sym==null) throw new ParseException("Bad keyword format: "+s);
			push( sym);
		}

		@Override
		public void enterAddress(AddressContext ctx) {
			// Nothing to do
		}

		@Override
		public void exitAddress(AddressContext ctx) {
			String s=ctx.getText();
			push (Address.parse(s));
		}

		@Override
		public void enterSyntax(SyntaxContext ctx) {
			// add new list to collect [syntax, value]
			pushList();
		}


		@Override
		public void exitSyntax(SyntaxContext ctx) {
			ArrayList<ACell> elements=popList();
			if (elements.size()!=2) throw new ParseException("Metadata requires metadata and annotated form but got:"+ elements);
			ACell value=elements.get(1);
			AHashMap<ACell,ACell> meta=ReaderUtils.interpretMetadata(elements.get(0));
			push(Syntax.create(value, meta));
		}

		@Override
		public void enterBlob(BlobContext ctx) {
			// Nothing to do
			
		}

		@Override
		public void exitBlob(BlobContext ctx) {
			String s=ctx.getText();
			Blob b=Blob.fromHex(s.substring(2));
			if (b==null) throw new ParseException("Invalid Blob syntax: "+s);
			push(b);
		}

		@Override
		public void enterQuoted(QuotedContext ctx) {
			// Nothing to do
			
		}

		@Override
		public void exitQuoted(QuotedContext ctx) {
			ACell form=pop();
			String qs=ctx.getStart().getText();
			Symbol qsym=ReaderUtils.getQuotingSymbol(qs);
			if (qsym==null) throw new ParseException("Invalid quoting reader macro: "+qs);
			push(Lists.of(qsym,form));	
		}
	}

	
	public static ACell read(String s) {
		ConvexLexer lexer=new ConvexLexer(CharStreams.fromString(s));
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ConvexParser parser = new ConvexParser(tokens);
		ParseTree tree = parser.form();
		
		CRListener visitor=new CRListener();
		ParseTreeWalker.DEFAULT.walk(visitor, tree);
		
		ArrayList<ACell> top=visitor.popList();
		if (top.size()!=1) {
			throw new ParseException("Bad parse output: "+top+" in code "+s);
		}
		
		return top.get(0);
	}

}
