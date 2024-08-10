package com.example.taxi.ui.home.order

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.order.Address
import com.example.taxi.domain.model.order.OrderData
import com.example.taxi.domain.model.order.updateTextView
import com.example.taxi.utils.ConversionUtil.calculateDistance
import com.example.taxi.utils.convertToCyrillic
import com.example.taxi.utils.setPriceCost

open class OrderAdapter(private val list: List<OrderData<Address>>, private val location: Location?, private val receiveItem : BottomSheetInterface) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressFromTextView: TextView = itemView.findViewById(R.id.addressFromTextView)
        private val addressToTextView: TextView = itemView.findViewById(R.id.addressToTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        private val distanceTextView: TextView = itemView.findViewById(R.id.distanceTextView)
        private val typeTextView: AppCompatTextView = itemView.findViewById(R.id.textView_type)
        private val orderTime: TextView = itemView.findViewById(R.id.order_time)
        private val infoForPriceTv: TextView = itemView.findViewById(R.id.infoForPriceTv)

        fun bind(order: OrderData<Address>) {

            addressFromTextView.convertToCyrillic(order.address.from)

            if (order.predict_cost != null && order.predict_distance != null) {
                infoForPriceTv.text = itemView.context.getString(R.string.taxminiy_narx)
                priceTextView.setPriceCost(order.predict_cost)

            } else {
                infoForPriceTv.text = itemView.context.getString(R.string.boshlang_ich_summa)
                priceTextView.setPriceCost(order.start_cost)

            }
            if (order.address.to.isEmpty()){
                addressToTextView.text = "-"
            }else{

                addressToTextView.convertToCyrillic(order.address.to)
            }

//            orderTime.text = order.created_at.time.substring(0,5)

            val lat2 = location?.latitude
            val long2 = location?.longitude

            val distance = lat2?.let {
                long2?.let { it1 ->
                    calculateDistance(
                        lat1 = order.latitude1.toDouble(),
                        lat2 = it,
                        lon1 = order.longitude1.toDouble(),
                        lon2 = it1
                    )
                }
            }
            distanceTextView.text = distance


            priceTextView.setPriceCost(order.start_cost)
            order.type?.let { updateTextView(it, typeTextView) }

            itemView.setOnClickListener {
                distance?.let { it1 -> receiveItem.showBottom(order, distance = it1) }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = list[position]
        holder.bind(order)
    }
}