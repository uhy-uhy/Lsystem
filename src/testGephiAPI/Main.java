/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package testGephiAPI;

import java.awt.Color;

import java.io.File;
import java.io.IOException;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.RankingController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

public class Main {

    public static void main(String[] args) {
    	Main main = new Main();
    	main.run2();

    }
    
    public void run3(){

    }
    
    public void run2(){
//    	HeadlessSimple headlessSimple = new HeadlessSimple();
//        headlessSimple.script();
//        
//        WithAutoLayout autoLayout = new WithAutoLayout();
//        autoLayout.script();
//
//        ParallelWorkspace parallelWorkspace = new ParallelWorkspace();
//        parallelWorkspace.script();
//
//        PartitionGraph partitionGraph = new PartitionGraph();
//        partitionGraph.script();
//
        RankingGraph rankingGraph = new RankingGraph();
        rankingGraph.script();
//
//        Filtering filtering = new Filtering();
//        filtering.script();
//
//        ImportExport importExport = new ImportExport();
//        importExport.script();
//
//        MYSQLImportExport mYSQLImportExport = new MYSQLImportExport();
//        mYSQLImportExport.script();
//
//        ManualGraph manualGraph = new ManualGraph();
//        manualGraph.script();
//
//        ManipulateAttributes manipulateAttributes = new ManipulateAttributes();
//        manipulateAttributes.script();
//
//        DynamicMetric longitudinalGraph = new DynamicMetric();
//        longitudinalGraph.script();
//
//        ImportDynamic importDynamic = new ImportDynamic();
//        importDynamic.script();
    	
//    	GenerateGraph generateGraph = new GenerateGraph();
//    	generateGraph.script();
    	
//    	PreviewJFrame previewJFrame = new PreviewJFrame();
//    	previewJFrame.script();
    }
    
  
}
