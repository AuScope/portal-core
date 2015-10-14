/**
 * 
 */
package org.auscope.portal.core.view.knownlayer;

import org.auscope.portal.core.view.knownlayer.KnownLayerSelector.RelationType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author u86990
 *
 */
public class RelationTypeTest {

    @Test
    public void testRelationTypeRelationships01() {
        RelationType left = RelationType.Belongs;
        RelationType right = RelationType.Related;
        
        Assert.assertTrue(left.ordinal() > right.ordinal());
    }

    @Test
    public void testRelationTypeRelationships02() {
        RelationType left = RelationType.Belongs;
        RelationType right = RelationType.NotRelated;
        
        Assert.assertTrue(left.ordinal() > right.ordinal());
    }
    
    @Test
    public void testRelationTypeRelationships03() {
        RelationType left = RelationType.Related;
        RelationType right = RelationType.NotRelated;
        
        Assert.assertTrue(left.ordinal() > right.ordinal());
    }
}
