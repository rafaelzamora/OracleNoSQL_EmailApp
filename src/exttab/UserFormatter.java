package exttab;

import email.to.UserTO;

import oracle.kv.KVStore;
import oracle.kv.exttab.TableFormatter;
import oracle.kv.table.Row;

//Commit de prueba de integracion
/**
 *  This class is used to format the User table row that has 'name' as the nested field:
 *   {"first":"foo","middle":"","last":"bar"} and because of the nested
 *  field we need to write our own custom formatter. For other tables like user.folder or sequence etc we can
 *  use out of the box formatter as they have simple data types only.
 * 
 *
 */
public class UserFormatter implements TableFormatter {
	
	public UserFormatter() {
		super();
	}

	@Override
	public String toOracleLoaderFormat(Row row, KVStore kvStore) {
		
		String rowStr = null;
		UserTO userTO = null;
		
		if(row!=null){
			userTO = new UserTO(row);
			if(userTO != null){
				rowStr = userTO.toString();
			}
		}
		return rowStr;
	}//toOracleLoaderFormat
	
	
}//UserFormatter
