<template>
	<view>
		<text>about</text>
		<button @click="goToIndex()">click</button>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				
			}
		},
		methods: {
			goToIndex(){
				uni.navigateTo({
					url:'/pages/index/index'
				})
			}
		}
	}
</script>

<style>

</style>
