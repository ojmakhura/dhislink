DROP TRIGGER IF EXISTS `lab_testing_resulting_complete_update_trigger`;

DELIMITER $$
 
create trigger lab_testing_resulting_complete_update_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
    declare count_ int default 1;
    
	declare report_max_event smallint(4);
	declare results varchar(255);
	declare test_assay_datetime varchar(255);
	declare test_assay_personnel varchar(255);
	declare test_assay_batchsize int;
	declare test_assay_batch_id varchar(255);
	declare c_value text;
    
	select 352 into lab_report;	
	select 362 into lab_receiving;	
	select 344 into lab_extraction;	
	select 345 into lab_results;
	
	-- Stop calling this procedure with any other data
	if (new.field_name = 'resulting_complete' and new.value = '2' and new.project_id = lab_results) then
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
                    			
		select value into test_assay_datetime
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_assay_datetime';
            			
		select value into test_assay_personnel
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_assay_personnel';
					
		select value into test_assay_batchsize
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_assay_batchsize';
		        
		select value into test_assay_batch_id
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_assay_batch_id';
		
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------	
        s2_loop:repeat
			
			-- Find the barcode
            select value
            into c_value
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_det_barcode_', count_);
			
			-- Now find the results
            select value
            into results
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_assay_result_', count_);
			
            -- Update the staging
			update redcap_ddp_specimen
			set TEST_ASSAY_BATCHSIZE = test_assay_batchsize,
				TEST_ASSAY_DATETIME = test_assay_datetime,
                TEST_ASSAY_PERSONNEL = test_assay_personnel,
                test_assay_batch_id = test_assay_batch_id,
                TEST_ASSAY_RESULT = results,
                RESULTS = results,
                RESULTS_ENTERED_BY = test_assay_personnel,
                RESULTS_ENTERED_DATE = test_assay_datetime
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_assay_batchsize
        end repeat;
	
	end if;
end; $$

DELIMITER ;

DROP TRIGGER IF EXISTS `lab_testing_resulting_complete_insert_trigger`;

DELIMITER $$
 
create trigger lab_testing_resulting_complete_insert_trigger
after insert
on redcap_data for each row
begin
	
    declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
    declare count_ int default 1;
    
	declare report_max_event smallint(4);
	declare results varchar(255);
	declare test_assay_datetime varchar(255);
	declare test_assay_personnel varchar(255);
	declare test_assay_batchsize int;
	declare test_assay_batch_id varchar(255);
	declare c_value text;
    
	select 352 into lab_report;	
	select 362 into lab_receiving;	
	select 344 into lab_extraction;	
	select 345 into lab_results;
	
	-- Stop calling this procedure with any other data
	if (new.field_name = 'resulting_complete' and new.value = '2' and new.project_id = lab_results) then
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
                    			
		select value into test_assay_datetime
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_assay_datetime';
            			
		select value into test_assay_personnel
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_assay_personnel';
					
		select value into test_assay_batchsize
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_assay_batchsize';
		        
		select value into test_assay_batch_id
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_assay_batch_id';
		
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------	
        s2_loop:repeat
			
			-- Find the barcode
            select value
            into c_value
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_det_barcode_', count_);
			
			-- Now find the results
            select value
            into results
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_assay_result_', count_);
			
            -- Update the staging
			update redcap_ddp_specimen
			set TEST_ASSAY_BATCHSIZE = test_assay_batchsize,
				TEST_ASSAY_DATETIME = test_assay_datetime,
                TEST_ASSAY_PERSONNEL = test_assay_personnel,
                test_assay_batch_id = test_assay_batch_id,
                TEST_ASSAY_RESULT = results,
                RESULTS = results,
                RESULTS_ENTERED_BY = test_assay_personnel,
                RESULTS_ENTERED_DATE = test_assay_datetime
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_assay_batchsize
        end repeat;
	
	end if;
	
end; $$

DELIMITER ;
