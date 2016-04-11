package paulpackage;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Arrays;

import junit.framework.Assert;


public class TestAcceptanceBasic extends TestCase
{
	private static SM s_smInstance;
	
	public TestAcceptanceBasic( String name )
	{
		
	    super( name );
	    System.out.println("Calling super of "+name);
	}
	
	public void testStore()
	{
		System.out.println("Inside test store++++++++++");
		try
		{
            SM.Record recordToStore = new SM.Record( 20 );
            SM.OID storedOID = s_smInstance.store( recordToStore );
            //byte[] a = (byte[]) storedOID;
            byte[] a = storedOID.toBytes();
           System.out.println("STOREID to bytes"+Arrays.toString(a));
            System.out.println("STOREID ==="+a.length);
            Assert.assertNotNull( storedOID );
		}
		catch( Exception e )
		{
            Assert.fail( e.getMessage() );
		}
	}

    public void testFetch()
    {
    	System.out.println("Inside test fetch++++++++++");
        try
        {
            SM.Record recordToStore = new SM.Record( 30 );
            SM.OID storedOID = s_smInstance.store( recordToStore );
            Assert.assertNotNull( storedOID );
            SM.Record found = s_smInstance.fetch( storedOID );
            Assert.assertNotNull( found );
        }
        catch( Exception e )
        {
            Assert.fail( e.getMessage() );
        }
    }

    public void testClose ()
    {
    	System.out.println("Inside test close++++++++++");
        try
        {
            s_smInstance.close();
            s_smInstance = null;
//            s_oid = null;
        }
        catch (Exception e)
        {
            Assert.fail( e.getMessage() );
        }
    }

    public void testUpdate()
    {
    	System.out.println("Inside test update++++++++++");
        try
        {
            SM.Record recordToStore = new SM.Record( 40 );
            SM.Record recordToUpdate = new SM.Record( 41 );
            SM.OID storedOID = s_smInstance.store( recordToStore );
            Assert.assertNotNull( storedOID );
            SM.OID updatedOID = s_smInstance.update( storedOID, recordToUpdate );
            Assert.assertNotNull( storedOID );
            SM.Record updatedRecord = s_smInstance.fetch( updatedOID );
            Assert.assertEquals( recordToUpdate, updatedRecord );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            Assert.fail( e.getMessage() );
        }
    }

    public void testDelete()
    {
    	System.out.println("Inside test delete++++++++++");
        try
        {
            boolean notBeFound = false;
            SM.Record recordToStore = new SM.Record( 42 );
            SM.OID storedOID = s_smInstance.store( recordToStore );
            s_smInstance.delete( storedOID );
            try
            {
                SM.Record shouldNotBeFound = s_smInstance.fetch( storedOID );
            }
            catch( SM.NotFoundException e )
            {
                notBeFound = true;
            }
            Assert.assertTrue( notBeFound );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            Assert.fail( e.getMessage() );
        }
    }

	protected void setUp()
	{ 
		System.out.println("setUp called");
		if ( s_smInstance == null )
		{
			s_smInstance = SMFactory.getInstance();
		}
	}

	static public Test suite ()
	{
		System.out.println("Test suite method+++++");
		TestSuite suite= new TestSuite();
		System.out.println("Calling addsuites method+++++");
		suite.addTest( new TestAcceptanceBasic( "testStore" ) );
		suite.addTest( new TestAcceptanceBasic( "testFetch" ) );
		suite.addTest( new TestAcceptanceBasic( "testUpdate" ) );
		suite.addTest( new TestAcceptanceBasic( "testDelete" ) );
		suite.addTest( new TestAcceptanceBasic( "testClose" ) );
		return suite;
	}

}
